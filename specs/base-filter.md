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

- Operator annotations beyond `@Like` and `@In` (e.g. `@NotEqual`, `@Not`, a standalone `@IsNotNull`). Relational comparisons are covered by `@From` / `@To`.
- Boolean composition beyond the single `@Or` disjunction: no named or nested groups. A filter needing them builds the predicate in a DAO hook.
- Matching one filter value against several attributes (e.g. a search box over name and email). That is custom logic, built in a DAO hook.
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

    private Map<String, Order> orders;        // insertion-ordered; getOrders() never returns null

    private Integer firstResult;              // null or >= 0
    private Integer maxResult;                // null or >= 0

    // Convenience mutators kept on the POJO surface
    public BaseFilter addOrder(String attribute);                 // defaults to ASC
    public BaseFilter addOrder(String attribute, Order direction);
    public BaseFilter setOrders(Map<String, Order> orders);           // copies the argument
    public Map<String, Order> getOrders();                            // unmodifiable view
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
- The inner `BaseFilterBuilder` is declared explicitly so that paging validation, a per-entry `.addOrder(...)` adder and a copying `.orders(Map)` setter are part of the builder API; Lombok fills in the rest (fields, `self()`, `build()`, the subclass plumbing).
- The `orders` map is never shared. Maps passed to `setOrders(...)` or the builder's `orders(...)` are copied, both `addOrder(...)` variants copy before writing, and `getOrders()` returns an unmodifiable view — so a filter built through `toBuilder()`, or built twice from the same builder, is independent of the others. POJO-style `addOrder(...)` still works after `build()`.
- `toBuilder = true` lets callers rebuild a tweaked copy of an existing filter (`f.toBuilder().maxResult(10).build()`), useful for paging.
- The sort-order attribute string follows the same dotted-path convention as `@Attribute` (see §3.2) so callers can sort across joins.
- Pagination validators throw `IllegalArgumentException` on negative values, matching `QuerySpec`. Validation lives in both the POJO setters and the corresponding builder methods (`firstResult(...)` / `maxResult(...)` on `BaseFilterBuilder`), so both construction styles enforce the same invariants.

### 3.2 Annotations (in `backend-core-model`, package `com.flowingcode.backendcore.model.filter`)

All annotations are field-level (`@Target(ElementType.FIELD)`) and retained at runtime. `@Attribute` maps the field to an entity attribute; at most one *operator* annotation (`@From`, `@To`, `@Like`, `@In`) chooses the predicate, which defaults to equality; `@WhenNull` and `@Or` adjust how the predicate is produced and combined.

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

- Maps a filter field to one entity attribute via a dotted path. Path traversal follows the same `split("\\.")` + auto-join convention used today in `ConstraintTransformerJpaImpl`; see §4.1 for the join types.
- A filter field with `@Attribute` and no role annotation defaults to **equality** (`cb.equal(...)`).
- `manual = true` is the escape hatch for predicates that don't fit the declarative model. The processor emits no predicate for the field; the DAO's `customizePredicates` hook is responsible for the constraint, reading the value through the filter's accessor (§5.1). On a manual field the `value()` is informational — the processor never resolves it — but stays useful as documentation of which entity attribute the hook is expected to target. `manual = true` cannot combine with `@From`, `@To`, `@Like`, `@In`, `@WhenNull` or `@Or`, since none of those have meaning when the predicate is hand-built.

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
- `IS_NULL` makes a null field emit `cb.isNull(...)` against the resolved attribute path. The associations on the path are left-joined, so `@Attribute("city.name")` matches both a city with a null name and a null city.
- Only meaningful on plain equality fields that also carry `@Attribute`; the processor rejects `@WhenNull` alongside `@From`, `@To`, `@Like` or `@In`.

#### `@Like`

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Like {
    boolean ignoreCase() default false;
    Match match() default Match.CONTAINS;

    enum Match { RAW, CONTAINS, STARTS_WITH, ENDS_WITH }
}
```

- Matches a `String` field with `LIKE` instead of equality. A non-`String` field is a configuration error.
- `CONTAINS` (default), `STARTS_WITH` and `ENDS_WITH` wrap the value with `%` and escape the `%` / `_` wildcards and the `\` escape character in it, so the value always matches literally. `RAW` passes the value through as the pattern, wildcards included — the behavior of the legacy `ConstraintBuilder.like(...)`.
- `ignoreCase = true` compares `lower(attribute)` with the value lower-cased in `Locale.ROOT`; it replaces the legacy `iLike(...)`.
- A null value is skipped.

#### `@In`

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface In {
    EmptyPolicy whenEmpty() default EmptyPolicy.SKIP;

    enum EmptyPolicy { SKIP, MATCH_NONE }
}
```

- Matches a `Collection` field with `attribute IN (...)`. A non-`Collection` field is a configuration error.
- A null value is skipped. An empty collection is skipped by default, treating an empty selection as "no criterion" like `null`; `MATCH_NONE` emits a predicate that is always false instead.

#### `@Or`

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Or { }
```

- Places the field's predicate in the filter's single disjunction instead of its conjunction: `WHERE <other fields> AND (<@Or fields>)`.
- Every `@Or` field of the filter, inherited ones included, joins the same disjunction. Fields that contribute no predicate (e.g. a null value) are left out; when none contributes, the disjunction is omitted — an empty disjunction would be false and match no rows.
- Associations on the path of an `@Or` field are left-joined, so a row with a null association can still match through another disjunct.
- Requires `@Attribute`, cannot be used on a `manual` field, and on a `@From` / `@To` pair must be on both fields or neither. Named or nested groups are out of scope (§2); they are built in a DAO hook.

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
2. **Validates** the annotations at first encounter: every other annotation requires `@Attribute`; `manual` fields take no other annotation; at most one operator (`@From`, `@To`, `@Like`, `@In`) per field; `@WhenNull` only on equality fields; `@Like` requires a `String` field and `@In` a `Collection`; `@Or` is on both fields of a range pair or neither; no two fields share an attribute path unless they form a `@From`/`@To` pair.
3. **Builds predicates** by walking the cached field metadata:
   - Single-attribute fields → equality.
   - `@Like` → `LIKE`, `@In` → `IN`, as described in §3.2.
   - `@From`-only / `@To`-only → comparison predicate honoring the field's `inclusive` flag (`>` / `>=` / `<` / `<=`).
   - `@From` + `@To` on the same `@Attribute` path → `cb.between(...)` only when both values are non-null **and** both bounds are inclusive; otherwise two ANDed comparison predicates with each side's `inclusive` setting honored independently. When only one side is non-null, emit just that side's comparison.
   - Null values are skipped unless `@WhenNull(IS_NULL)` is present.
   - Predicates of `@Or` fields are combined with `OR`; the result, the other predicates and the predicates from the `customizePredicates` hook are combined with `AND`.
4. **Resolves attribute paths** with `AttributePathResolver`, a package-private utility modeled on the path/join helper of `ConstraintTransformerJpaImpl` (split on `.`, auto-join). It is new code rather than an extraction: the deprecated transformer is left untouched, and the new processor does not depend on `Constraint` / `ConstraintTransformer`. Join types are described in §4.1.
5. **Applies sort orders** from `BaseFilter.getOrders()` in insertion order.
6. **Calls the DAO hook methods** (see §5) so subclasses can mutate the in-progress `CriteriaQuery`.
7. **Applies pagination** via `firstResult` / `maxResult` on the `TypedQuery`.

The pipeline is implemented twice in shape (once for the `T` result query, once for the `Long` count query), but the predicate assembly is shared. `filterWithSingleResult` builds the predicates without sort orders or paging and runs the query with `getSingleResult()`, returning an empty `Optional` for no match and throwing `IllegalStateException` for more than one.

### 4.1 Join types

Associations along a path are joined once per query: a join already present on the same attribute is reused whatever its type. The join type is chosen by the first resolution that creates it:

- **Inner** for equality, comparison, `LIKE` and `IN` predicates of non-`@Or` fields. These conjuncts reject null, so every result row has the association anyway. As a consequence, such a predicate on a nested path never matches rows whose association is null.
- **Left** where an inner join would drop the rows the query is meant to return: `IS NULL` predicates from `@WhenNull(IS_NULL)`, predicates of `@Or` fields, and sort orders (sorting must not change the result set, nor make `filter` disagree with `count`).

Reusing a join across types is safe because the declarative predicates are always combined as a conjunction: an inner join is only requested for a null-rejecting conjunct, and a left join behaves as an inner join under such a conjunct. The join type is inferred rather than configurable, so there is no join-type attribute on the annotations.

## 5. Extensibility — DAO hook methods

Customization happens on the DAO base, not on the filter. `ConversionJpaDaoSupport` (and by inheritance `JpaDaoSupport`) exposes hook methods that default to no-ops:

```java
/** Add predicates that don't fit the declarative model. Return null or empty to add nothing. */
default Collection<Predicate> customizePredicates(
        BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq, Root<T> root) {
    return Collections.emptyList();
}

/** Last-chance hook to mutate the CriteriaQuery (distinct, extra roots or joins, etc.) before execution. */
default void customizeCriteria(
        BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq, Root<T> root) {
    // no-op
}
```

Both hooks are called once per query (filter, count, single-result). They receive the same `CriteriaQuery` instance that the processor is building, so subclasses can call `cq.distinct(true)`, add `LEFT JOIN FETCH`-style joins (within Criteria limits), or attach predicates that the annotation model can't express (e.g. correlated subqueries, function calls, `OR` groups, one value matched against several attributes).

`customizeCriteria` must keep the query's selection — the entity root, or its count — since every query kind returns entities or a single count; replacing it (e.g. with a projection) fails with `IllegalStateException`. It must not add a `GROUP BY` to the count query either, which would return one count per group; the hook can recognize the count query by `cq.getResultType() == Long.class`. A `distinct(true)` set on the count query is honored as `COUNT(DISTINCT root)`, so the count agrees with the distinct list.

Why hooks and not annotation processors: it keeps the abstraction surface small and predictable, and it routes customization through the DAO — the layer that already owns the entity-shaped logic — instead of fanning custom behavior across filter classes.

### 5.1 Reading filter values in hooks

Hooks receive the filter as `BaseFilter` and read the values they need through the concrete filter class's accessors:

```java
if (filter instanceof PersonFilter f && f.getSearch() != null) {
    return List.of(cb.like(root.get("name"), "%" + f.getSearch() + "%"));
}
return List.of();
```

Filters must therefore provide accessors for every field a hook consumes, typically the fields marked `@Attribute(manual = true)`. The declarative fields need none: the processor reads them by reflection. There is deliberately no name-based lookup helper: a cast is checked by the compiler, so renaming a field breaks the hook at compile time rather than at run time, and the `instanceof` check is needed anyway when a DAO receives more than one filter class. Logic shared by several filter classes is expressed through a common superclass or interface (`filter instanceof HasTenant t`).

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

    @Attribute("lastName") @Like(ignoreCase = true)
    private String lastName;                // lower(lastName) LIKE '%:v%', wildcards in :v escaped

    @Attribute("status") @In
    private Set<Status> statuses;           // null or empty → skipped; else status IN (:v)

    @Attribute("email") @Or
    private String email;                   // with nickname: ... AND (email = :e OR nickname = :n)

    @Attribute("nickname") @Or
    private String nickname;

    @Attribute(value = "name", manual = true)
    private String search;                  // processor skips this; hook matches name or nickname
}

// DAO with a manual predicate
class PersonDao implements JpaDaoSupport<Person, Integer> {
    // ... getEntityManager() ...

    @Override
    public Collection<Predicate> customizePredicates(BaseFilter filter, CriteriaBuilder cb,
            CriteriaQuery<?> cq, Root<Person> root) {
        if (!(filter instanceof PersonFilter f) || f.getSearch() == null) {
            return List.of();
        }
        String pattern = "%" + f.getSearch() + "%";
        return List.of(cb.or(cb.like(root.get("name"), pattern),
                cb.like(root.get("nickname"), pattern)));
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
- The new processor resolves paths with its own non-deprecated `AttributePathResolver`, modeled on the path/join helper of `ConstraintTransformerJpaImpl`, so it does not depend on the deprecated transformer. The transformer itself is left unchanged.
- A follow-up issue tracks: (a) eventually flipping the deprecations to `forRemoval = true` once downstream usage is gone, (b) removing the deprecated DAO method overloads and the constraint types in a future major version.

## 8. Module placement

- `backend-core-model` — `BaseFilter`, `@Attribute`, `@From`, `@To`, `@Like`, `@In`, `@WhenNull`, `@Or`. Package: `com.flowingcode.backendcore.model.filter`.
- `backend-core-data` — new method signatures on `QueryDao`.
- `backend-core-data-impl` — `BaseFilterJpaProcessor`, default-method implementations on `ConversionJpaDaoSupport`, and the package-private `AttributePathResolver`.

## 9. Open questions

1. **Sort order via annotation.** Should a filter class be able to declare a default sort via annotation (e.g. `@DefaultSort("createdAt DESC")`)? Out of scope for v1; callers use `addOrder`.
2. **Validation timing.** Should the per-class reflection/validation run eagerly at startup (e.g. via a CDI extension or a Spring `BeanPostProcessor`) or lazily on first use? Spec assumes lazy with caching. Eager validation can be added later without API changes.
3. **Field discovery.** Inherited fields from a deeper hierarchy (filter extending filter) should be supported. Confirm whether non-public fields require `setAccessible(true)` allowances in target deployments.
4. **Builder validation hookup.** Resolved during implementation: the inner `BaseFilterBuilder` is declared explicitly with `@SuperBuilder` filling in the missing parts, and the `firstResult(...)` / `maxResult(...)` builder methods carry the same validation as the POJO setters.

## 10. Out of scope / follow-ups

- Further operator annotations (`@NotEqual`, `@IsNotNull` as a standalone, etc.). Revisit once usage patterns emerge.
- `@Not`, and `@Or` groups beyond the single disjunction.
- Pluggable / user-registered annotation processors.
- Removal of `QuerySpec` and friends.
- Projection / returned-attributes support equivalent to `QuerySpec.returnedAttributes`.
