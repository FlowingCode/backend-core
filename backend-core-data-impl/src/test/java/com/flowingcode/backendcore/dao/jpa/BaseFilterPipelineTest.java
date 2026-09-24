/*-
 * #%L
 * Commons Backend - Data Access Layer Implementations
 * %%
 * Copyright (C) 2020 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.backendcore.dao.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flowingcode.backendcore.model.filter.Attribute;
import com.flowingcode.backendcore.model.filter.BaseFilter;
import com.flowingcode.backendcore.model.filter.From;
import com.flowingcode.backendcore.model.filter.In;
import com.flowingcode.backendcore.model.filter.Like;
import com.flowingcode.backendcore.model.filter.Or;
import com.flowingcode.backendcore.model.filter.To;
import com.flowingcode.backendcore.model.filter.WhenNull;
import com.flowingcode.backendcore.model.impl.City;
import com.flowingcode.backendcore.model.impl.Person;

import lombok.Setter;
import lombok.experimental.Accessors;

/** End-to-end coverage of the annotation pipeline against an in-memory database. */
class BaseFilterPipelineTest {

	static class PersonDao implements JpaDaoSupport<Person, Integer> {

		final EntityManager em;
		final AtomicInteger rootJoins = new AtomicInteger(-1);

		PersonDao(EntityManager em) {
			this.em = em;
		}

		@Override
		public EntityManager getEntityManager() {
			return em;
		}

		@Override
		public void customizeCriteria(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
				Root<Person> root) {
			rootJoins.set(root.getJoins().size());
		}
	}

	private EntityManagerFactory emf;
	private EntityManager em;
	private PersonDao dao;

	@BeforeEach
	void setUp() {
		emf = Persistence.createEntityManagerFactory("person");
		EntityManager tx = emf.createEntityManager();
		tx.getTransaction().begin();
		City laPlata = new City();
		laPlata.setName("La Plata");
		laPlata.setPopulation(700_000);
		tx.persist(laPlata);
		City unnamed = new City();
		tx.persist(unnamed);
		persist(tx, "John", "Doe", laPlata);
		persist(tx, "Jane", "Doe", unnamed);
		persist(tx, "Alice", "Smith", null);
		persist(tx, "johnny", "smith", null);
		persist(tx, "Ann_B", "Roe", null);
		persist(tx, "AnnXB", "Roe", null);
		tx.getTransaction().commit();
		tx.close();

		em = emf.createEntityManager();
		dao = new PersonDao(em);
	}

	private static void persist(EntityManager tx, String name, String lastName, City city) {
		Person p = new Person();
		p.setName(name);
		p.setLastName(lastName);
		p.setCity(city);
		tx.persist(p);
	}

	@AfterEach
	void tearDown() {
		if (em != null && em.isOpen()) {
			em.close();
		}
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}

	/** Asserts the people matched by {@code filter}, and that {@code count()} agrees. */
	private void assertMatches(BaseFilter filter, String... expectedNames) {
		List<String> actual = dao.filter(filter).stream()
				.map(Person::getName)
				.sorted()
				.collect(Collectors.toList());
		assertEquals(Stream.of(expectedNames).sorted().collect(Collectors.toList()), actual);
		assertEquals(expectedNames.length, dao.count(filter), "count() must agree with filter()");
	}

	private static final String[] EVERYONE = {"John", "Jane", "Alice", "johnny", "Ann_B", "AnnXB"};

	// ----- @Like -----

	@Setter
	@Accessors(chain = true)
	static class LikeFilter extends BaseFilter {
		@Attribute("name") @Like
		private String nameContains;

		@Attribute("lastName") @Like(ignoreCase = true, match = Like.Match.STARTS_WITH)
		private String lastNameStartsIgnoringCase;

		@Attribute("city.name") @Like(match = Like.Match.ENDS_WITH)
		private String cityEnds;
	}

	@Setter
	@Accessors(chain = true)
	static class RawLikeFilter extends BaseFilter {
		@Attribute("name") @Like(match = Like.Match.RAW)
		private String namePattern;

		@Attribute("lastName") @Like(match = Like.Match.STARTS_WITH)
		private String lastNameStarts;
	}

	@Test
	void like_containsByDefault_andIsCaseSensitive() {
		assertMatches(new LikeFilter().setNameContains("Joh"), "John");
	}

	@Test
	void like_ignoreCase_lowerCasesBothSides() {
		assertMatches(new LikeFilter().setLastNameStartsIgnoringCase("SMI"), "Alice", "johnny");
		assertMatches(new RawLikeFilter().setLastNameStarts("Smi"), "Alice");
	}

	@Test
	void like_endsWith_onNestedPath() {
		assertMatches(new LikeFilter().setCityEnds("Plata"), "John");
	}

	@Test
	void like_escapesWildcardsInTheValue() {
		// Unescaped, "_" would also match "AnnXB" and "%" would match everyone.
		assertMatches(new LikeFilter().setNameContains("n_B"), "Ann_B");
		assertMatches(new LikeFilter().setNameContains("%"));
	}

	@Test
	void like_raw_keepsWildcards() {
		assertMatches(new RawLikeFilter().setNamePattern("Ann_B"), "Ann_B", "AnnXB");
	}

	@Test
	void like_nullValue_isSkipped() {
		assertMatches(new LikeFilter(), EVERYONE);
	}

	// ----- @In -----

	@Setter
	@Accessors(chain = true)
	static class InFilter extends BaseFilter {
		@Attribute("lastName") @In
		private List<String> lastNames;

		@Attribute("name") @In(whenEmpty = In.EmptyPolicy.MATCH_NONE)
		private List<String> names;
	}

	@Test
	void in_matchesAnyElement() {
		assertMatches(new InFilter().setLastNames(List.of("Doe", "Roe")), "John", "Jane", "Ann_B", "AnnXB");
	}

	@Test
	void in_emptyCollection_isSkippedByDefault() {
		assertMatches(new InFilter().setLastNames(List.of()), EVERYONE);
	}

	@Test
	void in_emptyCollection_matchesNoneWhenConfigured() {
		assertMatches(new InFilter().setNames(List.of()));
	}

	@Test
	void in_nullValue_isSkipped() {
		assertMatches(new InFilter(), EVERYONE);
	}

	// ----- @Or -----

	@Setter
	@Accessors(chain = true)
	static class OrFilter extends BaseFilter {
		@Attribute("city.name") @Or
		private String cityName;

		@Attribute("name") @Or
		private String name;

		@Attribute("lastName")
		private String lastName;
	}

	@Test
	void or_matchesThroughAnyDisjunct_includingRowsWithANullAssociation() {
		// Alice has no city: an inner join on city would drop her before the OR runs.
		assertMatches(new OrFilter().setCityName("La Plata").setName("Alice"), "John", "Alice");
	}

	@Test
	void or_disjunctionIsAndedWithTheOtherFields() {
		assertMatches(new OrFilter().setCityName("La Plata").setName("Alice").setLastName("Smith"),
				"Alice");
	}

	@Test
	void or_withoutAnyValue_doesNotRestrict() {
		// An empty disjunction is false; it must be omitted rather than match nothing.
		assertMatches(new OrFilter(), EVERYONE);
	}

	@Test
	void or_singleValue_actsAsThatPredicate() {
		assertMatches(new OrFilter().setName("Alice"), "Alice");
	}

	// ----- join types -----

	@Setter
	@Accessors(chain = true)
	static class CityNameFilter extends BaseFilter {
		@Attribute("city.name") @WhenNull(WhenNull.Policy.IS_NULL)
		private String cityName;
	}

	@Test
	void isNull_onNestedPath_matchesNullAssociations() {
		// Jane's city has no name; the other four have no city at all.
		assertMatches(new CityNameFilter(), "Jane", "Alice", "johnny", "Ann_B", "AnnXB");
	}

	@Test
	void isNullPolicy_withValue_isPlainEquality() {
		assertMatches(new CityNameFilter().setCityName("La Plata"), "John");
	}

	@Test
	void orderingByNestedPath_doesNotDropRows() {
		OrFilter filter = new OrFilter();
		filter.addOrder("city.name");
		assertMatches(filter, EVERYONE);
	}

	@Setter
	@Accessors(chain = true)
	static class SharedJoinFilter extends BaseFilter {
		@Attribute("city.name")
		private String cityName;

		@Attribute("city.population") @Or
		private Integer population;

		@Attribute("name") @Or
		private String name;
	}

	@Test
	void associationUsedByAndAndOrFields_isJoinedOnce() {
		SharedJoinFilter filter = new SharedJoinFilter()
				.setCityName("La Plata").setPopulation(700_000).setName("Nobody");
		filter.addOrder("city.name");
		assertMatches(filter, "John");
		assertEquals(1, dao.rootJoins.get(), "city must be joined once");
	}

	// ----- field discovery -----

	static class NameFilter extends BaseFilter {
		@Attribute("name")
		private String name;
	}

	static class ShadowingFilter extends NameFilter {
		@Attribute("lastName")
		private String name;
	}

	@Test
	void annotatedFieldShadowedBySubclassField_isStillReadable() {
		assertMatches(new ShadowingFilter(), EVERYONE);
	}

	// ----- configuration errors -----

	static class LikeOnNonString extends BaseFilter {
		@Attribute("city.population") @Like
		private Integer population;
	}

	static class InOnNonCollection extends BaseFilter {
		@Attribute("name") @In
		private String name;
	}

	static class LikeWithFrom extends BaseFilter {
		@Attribute("name") @Like @From
		private String name;
	}

	static class InWithWhenNull extends BaseFilter {
		@Attribute("lastName") @In @WhenNull(WhenNull.Policy.IS_NULL)
		private List<String> lastNames;
	}

	static class OrWithoutAttribute extends BaseFilter {
		@Or
		private String name;
	}

	static class OrOnManualField extends BaseFilter {
		@Attribute(value = "name", manual = true) @Or
		private String name;
	}

	static class OrOnOneSideOfRange extends BaseFilter {
		@Attribute("city.population") @From @Or
		private Integer populationFrom;

		@Attribute("city.population") @To
		private Integer populationTo;
	}

	private void assertRejected(BaseFilter filter, String expectedInMessage) {
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.count(filter));
		assertTrue(ex.getMessage().contains(expectedInMessage),
				"message should mention " + expectedInMessage + "; got: " + ex.getMessage());
	}

	@Test
	void invalidConfigurations_areRejected() {
		assertRejected(new LikeOnNonString(), "must be String");
		assertRejected(new InOnNonCollection(), "must be a Collection");
		assertRejected(new LikeWithFrom(), "cannot combine @From and @Like");
		assertRejected(new InWithWhenNull(), "cannot combine @WhenNull with @In");
		assertRejected(new OrWithoutAttribute(), "has @Or but no @Attribute");
		assertRejected(new OrOnManualField(), "manual=true");
		assertRejected(new OrOnOneSideOfRange(), "must either both carry @Or or neither");
	}
}
