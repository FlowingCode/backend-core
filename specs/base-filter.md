# BaseFilter — Annotation-driven JPA Criteria filtering

Status: Draft
Branch: `base-filter`
Author: Leo
Module: `backend-core-model` (abstraction + annotations), `backend-core-data-impl` (JPA processor + DAO hooks)

## 1. Motivation

The current filtering abstraction (`QuerySpec` + `Constraint` hierarchy + `ConstraintTransformer`) is imperative and verbose: callers must construct `Constraint` objects, register them on a `QuerySpec`, and let `ConstraintTransformerJpaImpl` turn them into JPA `Predicate`s. It works, but every new query surface in a downstream project ends up reimplementing the same boilerplate.

`BaseFilter` replaces that with a *declarative* model: application developers define a POJO whose fields describe the filterable criteria, annotate the fields with lightweight metadata, and pass the populated instance to the DAO. The DAO base implementation reflects on the filter, builds a JPA `CriteriaQuery`, and runs it. The existing `QuerySpec`-based DAO methods — and the supporting `QuerySpec` / `Constraint` / `ConstraintTransformer` types — are deprecated as part of this change but **not** marked for removal; downstream consumers can migrate at their own pace.

This spec covers the abstract class, the initial annotation set, the criteria-building pipeline in the DAO base, and the extensibility surface.

## 2. Goals and non-goals

### Goals

- A single abstract `BaseFilter` class that downstream filters extend.
- Filter fields describe *what entity attribute they map to* and *what semantic role* the field plays (single value, lower bound, upper bound). The operator is inferred from the metadata, not declared per field.
- Built-in support for sort orders, first result, and max result, mirroring `QuerySpec`'s pagination semantics.
- Two equally supported construction styles on every filter: a **POJO style** (no-arg constructor + chainable setters) and a **Builder style** (typed, inheritance-aware builder, populated by `Filter.builder()...build()`).
- A JPA `CriteriaQuery` builder living in the DAO base, with well-defined hook methods so subclasses can add predicates/joins/projections that don't fit the declarative model.
- Coexist with `QuerySpec`-based DAO methods during a deprecation window.

### Non-goals (v1)

- Operator-specific annotations (`@Like`, `@ILike`, `@GreaterThan`, `@In`, `@IsNull`, etc.). The first cut ships only *metadata* annotations that describe the role of the field; operator-specific annotations will be revisited once the abstraction proves out.
- Boolean composition annotations (`@Or`, `@Not`).
- Pluggable annotation processors (registering third-party annotations). Extensibility is via DAO hook methods only.
- Reusing or wrapping `QuerySpec`, `Constraint`, or `ConstraintTransformer`. The new pipeline is independent.
- Replacing `findById` / `findAll` / `save` / `update` / `delete`. Only the filter-shaped methods are touched.

## 3. Public API surface

### 3.1 `BaseFilter` (in `backend-core-model`)

```java
package com.flowingcode.backendcore.model.filter;

@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class BaseFilter {

    public enum Order { ASC, DESC }

    @Singular("order")
    private Map<String, Order> orders;        // initialized to LinkedHashMap; never null

    private Integer firstResult;              // null or >= 0
    private Integer maxResult;                // null or >= 0

    // Convenience mutators kept on the POJO surface
    public BaseFilter addOrder(String attribute);                 // defaults to ASC
    public BaseFilter addOrder(String attribute, Order direction);
}
```

Two construction styles are supported, and both populate the same underlying state:

```java
// POJO style — no-arg ctor + chainable setters
PersonFilter f = new PersonFilter()
        .setName("Ada")
        .setBirthDateFrom(LocalDate.of(1990, 1, 1));
f.addOrder("name");
f.setMaxResult(50);

// Builder style — Lombok @SuperBuilder
PersonFilter f = PersonFilter.builder()
        .name("Ada")
        .birthDateFrom(LocalDate.of(1990, 1, 1))
        .addOrder("name", BaseFilter.Order.ASC)
        .maxResult(50)
        .build();
```

Notes on the dual-style design:

- Lombok's `@SuperBuilder` is the simplest way to produce a typed builder that correctly composes with inheritance — subclasses opt in by adding `@SuperBuilder` and their builder inherits the base fields automatically. The existing codebase already relies on Lombok (`@Getter`/`@Setter`/`@Accessors(chain=true)` on `QuerySpec`), so this matches established conventions.
- The chainable POJO setters (via `@Accessors(chain = true)`) are kept so callers who already work with mutable filters — including code wired through frameworks that prefer no-arg construction + property binding (e.g. JSON deserialization, query-param binding) — don't pay any extra ceremony.
- The inner `BaseFilterBuilder` is declared explicitly so that paging validation and a per-entry `.addOrder(...)` adder are part of the builder API; Lombok fills in the rest (fields, `self()`, `build()`, the subclass plumbing). The built `orders` map is a `LinkedHashMap` (mutable, insertion-ordered) so subsequent POJO-style `addOrder(...)` calls still work after `build()`.
- `toBuilder = true` lets callers rebuild a tweaked copy of an existing filter (`f.toBuilder().maxResult(10).build()`), useful for paging.
- The sort-order attribute string follows the same dotted-path convention as `@Attribute` (see §3.2) so callers can sort across joins.
- Pagination validators throw `IllegalArgumentException` on negative values, matching `QuerySpec`. Validation lives in both the POJO setters and the corresponding builder methods (`firstResult(...)` / `maxResult(...)` on `BaseFilterBuilder`), so both construction styles enforce the same invariants.

### 3.2 Annotations (in `backend-core-model`, package `com.flowingcode.backendcore.model.filter`)

All annotations are field-level (`@Target(ElementType.FIELD)`), retained at runtime, and purely *metadata* — they describe how a filter field relates to the entity, not the operator to apply.

#### `@Attribute`

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Attribute {
    /** Dotted attribute path on the target entity, e.g. "city.state.name". */
    String value();

    /** When true, predicate building is the hook's responsibility. */
    boolean manual() default false;
}
```

- Maps a filter field to one or more entity attributes via a dotted path. Path traversal follows the same `split("\\.")` + auto-join convention used today in `ConstraintTransformerJpaImpl`, including the inner-join-by-default behavior.
- A filter field with `@Attribute` and no role annotation defaults to **equality** (`cb.equal(...)`).
- `manual = true` is the escape hatch for predicates that don't fit the declarative model. The processor records the field (so the value-accessor helper can read it) but emits no predicate; the DAO's `customizePredicates` hook is responsible for the constraint. On a manual field the `value()` is informational — the processor never resolves it — but stays useful as documentation of which entity attribute the hook is expected to target. `manual = true` cannot combine with `@From`, `@To`, or `@WhenNull`, since none of those have meaning when the predicate is hand-built.

#### `@From` and `@To`

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface From {
    /** When true (default), the lower bound is inclusive (>=); when false, strict (>). */
    boolean inclusive() default true;
}

@Retention(RUNTIME) @Target(FIELD)
public @interface To {
    /** When true (default), the upper bound is inclusive (<=); when false, strict (<). */
    boolean inclusive() default true;
}
```

- `@From` marks the field as the **lower bound** of a range comparison on its `@Attribute`; `inclusive` controls whether the comparison is `>=` (default) or `>`.
- `@To` marks the field as the **upper bound**; `inclusive` controls `<=` (default) or `<`.
- Single-sided behavior:
  - `@From` alone → `>=` or `>` depending on `inclusive`.
  - `@To` alone → `<=` or `<` depending on `inclusive`.
- Paired behavior — when the same `@Attribute("x")` value appears on two fields, one with `@From` and the other with `@To`:
  - **Both bounds inclusive and both values non-null** → emit a single `cb.between(...)` predicate (BETWEEN is inclusive on both sides in JPA/SQL).
  - **Any bound exclusive, or mixed inclusivity** → fall back to two predicates ANDed together (`> lower AND < upper`, `>= lower AND < upper`, etc.). The BETWEEN optimization is dropped because JPA `between` cannot express exclusivity.
  - **Only one side non-null** → emit the single available comparison using that side's `inclusive` setting; the other side contributes nothing.
- `@From` / `@To` without `@Attribute` is a configuration error and must fail fast at startup or on first use, with a clear message.

#### `@WhenNull`

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface WhenNull {
    Policy value();

    enum Policy { SKIP, IS_NULL }
}
```

- Per-field override for null handling. Default is `SKIP` (no predicate emitted), so most filter fields don't need this annotation.
- `IS_NULL` makes a null field emit `cb.isNull(...)` against the resolved attribute path.
- Only meaningful on fields that also carry `@Attribute`. On `@From` / `@To`, `SKIP` is the only sensible policy and the processor must reject `IS_NULL` with a clear error.

### 3.3 DAO surface changes (in `backend-core-data` and `backend-core-data-impl`)

Add overloads to `QueryDao` and `ConversionJpaDaoSupport` that take a `BaseFilter`:

```java
// backend-core-data, com.flowingcode.backendcore.dao.QueryDao
List<T> filter(BaseFilter filter);
Optional<T> filterWithSingleResult(BaseFilter filter);
long count(BaseFilter filter);
```

The existing `QuerySpec` overloads stay but are annotated `@Deprecated(forRemoval = false)` with javadoc pointing at `BaseFilter`. Removal is deferred to a future major version.

`ConversionJpaDaoSupport` provides default implementations that delegate to a `BaseFilterJpaProcessor` (see §4) and convert results via the existing `convertFrom`. `JpaDaoSupport` inherits transparently.

## 4. Criteria-building pipeline

A new package-private (or `protected`-accessible) class lives alongside the existing `FilterProcesor`:

```java
// backend-core-data-impl
class BaseFilterJpaProcessor<T extends Identifiable<K>, K extends Serializable> { ... }
```

For each call, the processor:

1. **Reflects** the `BaseFilter` subclass to discover annotated fields. Reflection results (field handles, attribute paths, role classification, null policy) are **cached per filter class** in a static map to avoid re-walking the class on every query.
2. **Validates** the annotations at first encounter (range-pair consistency, `@From`/`@To` requires `@Attribute`, `@WhenNull(IS_NULL)` requires `@Attribute` and forbids `@From`/`@To`, no duplicate single-value `@Attribute` on the same path, range pairs share the exact same attribute path).
3. **Builds predicates** by walking the cached field metadata:
   - Single-attribute fields → equality.
   - `@From`-only / `@To`-only → comparison predicate honoring the field's `inclusive` flag (`>` / `>=` / `<` / `<=`).
   - `@From` + `@To` on the same `@Attribute` path → `cb.between(...)` only when both values are non-null **and** both bounds are inclusive; otherwise two ANDed comparison predicates with each side's `inclusive` setting honored independently. When only one side is non-null, emit just that side's comparison.
   - Null values are skipped unless `@WhenNull(IS_NULL)` is present.
4. **Resolves attribute paths** via the same auto-joining strategy used today in `ConstraintTransformerJpaImpl` (split on `.`, reuse existing joins when the join type matches). The path/join helper should be **extracted into a shared utility** so both the legacy transformer and the new processor consume it, but the new processor does not depend on `Constraint` / `ConstraintTransformer`.
5. **Applies sort orders** from `BaseFilter.getOrders()` in insertion order.
6. **Calls the DAO hook methods** (see §5) so subclasses can mutate the in-progress `CriteriaQuery`.
7. **Applies pagination** via `firstResult` / `maxResult` on the `TypedQuery`.

The pipeline is implemented twice in shape (once for the `T` result query, once for the `Long` count query), but the predicate assembly is shared. `filterWithSingleResult` runs the standard list query and enforces single-result semantics, matching the existing `FilterProcesor` behavior.

## 5. Extensibility — DAO hook methods

Customization happens on the DAO base, not on the filter. `ConversionJpaDaoSupport` (and by inheritance `JpaDaoSupport`) exposes hook methods that default to no-ops:

```java
/** Add predicates that don't fit the declarative model. Return null or empty to add nothing. */
default Collection<Predicate> customizePredicates(
        BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq, Root<T> root) {
    return Collections.emptyList();
}

/** Last-chance hook to mutate the CriteriaQuery (joins, projections, group by, distinct, etc.) before execution. */
default void customizeCriteria(
        BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq, Root<T> root) {
    // no-op
}
```

Both hooks are called once per query (filter, count, single-result). They receive the same `CriteriaQuery` instance that the processor is building, so subclasses can call `cq.distinct(true)`, add `LEFT JOIN FETCH`-style joins (within Criteria limits), or attach predicates that the annotation model can't express (e.g. correlated subqueries, function calls).

Why hooks and not annotation processors: it keeps the abstraction surface small and predictable, and it routes customization through the DAO — the layer that already owns the entity-shaped logic — instead of fanning custom behavior across filter classes.

### 5.1 Value-accessor helper

Hooks frequently need to read filter field values to build predicates by hand. To avoid forcing every implementer to maintain its own reflection logic, the DAO base exposes:

```java
default Object getFilterFieldValue(BaseFilter filter, String fieldName);
default <V> V getFilterFieldValue(BaseFilter filter, String fieldName, Class<V> type);
```

Both overloads are backed by the same cached reflection used to build the declarative predicates, so the lookup is essentially a map read. The helper works on any field declared on the filter (annotated or not); it is especially useful for fields marked `@Attribute(manual = true)`, where the declarative path has been intentionally skipped.

## 6. Usage example

```java
@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class PersonFilter extends BaseFilter {

    @Attribute("name")
    private String name;                    // null → skipped; non-null → name = :v

    @Attribute("birthDate") @From
    private LocalDate birthDateFrom;        // null → skipped; non-null → birthDate >= :v

    @Attribute("birthDate") @To(inclusive = false)
    private LocalDate birthDateToExclusive; // pairs with birthDateFrom; strict upper bound

    @Attribute("address.city.name")
    private String cityName;                // auto-joins address → city, equality on name

    @Attribute("deletedAt") @WhenNull(WhenNull.Policy.IS_NULL)
    private Instant deletedAt;              // null → deletedAt IS NULL; non-null → equality

    @Attribute(value = "nickname", manual = true)
    private String nicknameLike;            // processor skips this; hook builds the LIKE
}

// DAO with a manual predicate
class PersonDao implements JpaDaoSupport<Person, Integer> {
    // ... getEntityManager() ...

    @Override
    public Collection<Predicate> customizePredicates(BaseFilter filter, CriteriaBuilder cb,
            CriteriaQuery<?> cq, Root<Person> root) {
        String pattern = getFilterFieldValue(filter, "nicknameLike", String.class);
        return pattern == null ? List.of()
                : List.of(cb.like(root.get("nickname"), "%" + pattern + "%"));
    }
}

// POJO style
PersonFilter f1 = new PersonFilter()
        .setBirthDateFrom(LocalDate.of(1990, 1, 1));
f1.addOrder("name");
f1.setMaxResult(50);

// Builder style
PersonFilter f2 = PersonFilter.builder()
        .birthDateFrom(LocalDate.of(1990, 1, 1))
        .addOrder("name", BaseFilter.Order.ASC)
        .maxResult(50)
        .build();

List<Person> people = personDao.filter(f2);
```

With both bounds set on `birthDate`, the processor emits `birthDate >= :from AND birthDate < :to` rather than a `BETWEEN`, because the upper bound is exclusive.

## 7. Deprecation plan for `QuerySpec`

- All `QuerySpec`-typed DAO method overloads on `QueryDao` and `ConversionJpaDaoSupport` are marked `@Deprecated(forRemoval = false)` with javadoc pointing at the `BaseFilter` overloads.
- `QuerySpec`, the `Constraint` hierarchy (`Constraint`, `ConstraintBuilder`, every concrete `Attribute*Constraint` and `DisjunctionConstraint` / `NegatedConstraint` / `RelationalConstraint`), and the transformer types (`ConstraintTransformer`, `ConstraintTransformerJpaImpl`, `ConstraintTransformerException`) are also marked `@Deprecated(forRemoval = false)` in this change. Nothing is scheduled for removal yet — downstream code can keep compiling and running, just with deprecation warnings.
- Internally, the path/join helper inside `ConstraintTransformerJpaImpl` is **extracted to a non-deprecated utility** so the new processor can consume it without depending on the deprecated transformer. The deprecated transformer keeps working by delegating to that utility.
- A follow-up issue tracks: (a) eventually flipping the deprecations to `forRemoval = true` once downstream usage is gone, (b) removing the deprecated DAO method overloads and the constraint types in a future major version.

## 8. Module placement

- `backend-core-model` — `BaseFilter`, `@Attribute`, `@From`, `@To`, `@WhenNull`. Package: `com.flowingcode.backendcore.model.filter`.
- `backend-core-data` — new method signatures on `QueryDao`.
- `backend-core-data-impl` — `BaseFilterJpaProcessor`, default-method implementations on `ConversionJpaDaoSupport`, shared path/join utility extracted from `ConstraintTransformerJpaImpl`.

## 9. Open questions

1. **Sort order via annotation.** Should a filter class be able to declare a default sort via annotation (e.g. `@DefaultSort("createdAt DESC")`)? Out of scope for v1; callers use `addOrder`.
2. **Validation timing.** Should the per-class reflection/validation run eagerly at startup (e.g. via a CDI extension or a Spring `BeanPostProcessor`) or lazily on first use? Spec assumes lazy with caching. Eager validation can be added later without API changes.
3. **Field discovery.** Inherited fields from a deeper hierarchy (filter extending filter) should be supported. Confirm whether non-public fields require `setAccessible(true)` allowances in target deployments.
4. **Builder validation hookup.** Resolved during implementation: the inner `BaseFilterBuilder` is declared explicitly with `@SuperBuilder` filling in the missing parts, and the `firstResult(...)` / `maxResult(...)` builder methods carry the same validation as the POJO setters.

## 10. Out of scope / follow-ups

- Operator-specific annotations (`@Like`, `@ILike`, `@In`, `@IsNull` as a standalone, `@Gt`/`@Lt`, etc.). Revisit once usage patterns emerge.
- Boolean composition (`@Or` groups, `@Not`).
- Pluggable / user-registered annotation processors.
- Removal of `QuerySpec` and friends.
- Projection / returned-attributes support equivalent to `QuerySpec.returnedAttributes`.
