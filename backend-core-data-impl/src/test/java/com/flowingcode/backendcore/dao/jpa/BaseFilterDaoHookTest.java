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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flowingcode.backendcore.model.filter.Attribute;
import com.flowingcode.backendcore.model.filter.BaseFilter;
import com.flowingcode.backendcore.model.filter.From;
import com.flowingcode.backendcore.model.impl.City;
import com.flowingcode.backendcore.model.impl.Person;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

class BaseFilterDaoHookTest {

	@Getter
	@Setter
	@Accessors(chain = true)
	@NoArgsConstructor
	@SuperBuilder
	static class PersonFilter extends BaseFilter {
		// Empty: this suite exercises the DAO hooks, not the annotation pipeline.
	}

	static class HookDao implements JpaDaoSupport<Person, Integer> {

		final EntityManager em;
		final AtomicInteger predicatesCalls = new AtomicInteger();
		final AtomicInteger criteriaCalls = new AtomicInteger();
		final AtomicBoolean restrictByName = new AtomicBoolean();

		HookDao(EntityManager em) {
			this.em = em;
		}

		@Override
		public EntityManager getEntityManager() {
			return em;
		}

		// Tests subclass this DAO anonymously, and the default reflective lookup only
		// inspects the interfaces implemented directly by the runtime class.
		@Override
		public Class<Person> getPersistentClass() {
			return Person.class;
		}

		@Override
		public Collection<Predicate> customizePredicates(BaseFilter filter, CriteriaBuilder cb,
				CriteriaQuery<?> cq, Root<Person> root) {
			predicatesCalls.incrementAndGet();
			if (!restrictByName.get()) {
				return Collections.emptyList();
			}
			List<Predicate> ps = new ArrayList<>();
			ps.add(cb.equal(root.get("name"), "John"));
			return ps;
		}

		@Override
		public void customizeCriteria(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
				Root<Person> root) {
			criteriaCalls.incrementAndGet();
		}
	}

	private HookDao dao;
	private EntityManagerFactory emf;
	private EntityManager em;

	@BeforeEach
	void setUp() {
		emf = Persistence.createEntityManagerFactory("person");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		for (String name : new String[] {"John", "Jane", "Alice", "John"}) {
			Person p = new Person();
			p.setName(name);
			p.setLastName("Doe");
			em.persist(p);
		}
		em.getTransaction().commit();
		em.close();
		// One EntityManager for all the DAOs of a test, like one per transaction under Spring.
		this.em = emf.createEntityManager();
		dao = new HookDao(this.em);
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

	@Test
	void hooksFireOnFilterAndCount() {
		PersonFilter f = PersonFilter.builder().build();
		dao.predicatesCalls.set(0);
		dao.criteriaCalls.set(0);

		dao.filter(f);
		dao.count(f);

		assertEquals(2, dao.predicatesCalls.get(),
				"customizePredicates should fire on filter() and count()");
		assertEquals(2, dao.criteriaCalls.get(),
				"customizeCriteria should fire on filter() and count()");
	}

	@Test
	void hookFiresOnFilterWithSingleResult() {
		// Restrict to a name with exactly one match so the call succeeds.
		dao.restrictByName.set(true);
		// The data has two "John"s, so override the restriction inline.
		HookDao d = new HookDao(em) {
			@Override
			public Collection<Predicate> customizePredicates(BaseFilter filter, CriteriaBuilder cb,
					CriteriaQuery<?> cq, Root<Person> root) {
				predicatesCalls.incrementAndGet();
				return List.of(cb.equal(root.get("name"), "Jane"));
			}
		};
		d.filterWithSingleResult(PersonFilter.builder().build());
		assertEquals(1, d.predicatesCalls.get());
		assertEquals(1, d.criteriaCalls.get());
	}

	@Test
	void predicateHookRestrictsResults() {
		PersonFilter f = PersonFilter.builder().build();

		long unrestricted = dao.count(f);
		assertEquals(4, unrestricted);

		dao.restrictByName.set(true);
		long restricted = dao.count(f);
		assertEquals(2, restricted, "predicate hook should restrict to two Johns");

		List<Person> people = dao.filter(f);
		assertEquals(2, people.size());
		assertTrue(people.stream().allMatch(p -> "John".equals(p.getName())));
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	@NoArgsConstructor
	@SuperBuilder
	static class ManualPersonFilter extends BaseFilter {

		// Manual field: the processor never produces a predicate for it. The hook
		// reads the value through the getter and builds the predicate itself.
		@Attribute(value = "name", manual = true)
		private String nameLike;
	}

	@Test
	void manualField_isSkipped_andHookReadsItThroughTheGetter() {
		HookDao d = new HookDao(em) {
			@Override
			public Collection<Predicate> customizePredicates(BaseFilter filter, CriteriaBuilder cb,
					CriteriaQuery<?> cq, Root<Person> root) {
				predicatesCalls.incrementAndGet();
				if (filter instanceof ManualPersonFilter f && f.getNameLike() != null) {
					return List.of(cb.like(root.get("name"), f.getNameLike()));
				}
				return Collections.emptyList();
			}
		};

		// No pattern set → no extra predicate → all four people returned.
		assertEquals(4, d.count(ManualPersonFilter.builder().build()));

		// Pattern set → hook builds the LIKE → matches the two "John"s.
		ManualPersonFilter f = ManualPersonFilter.builder().nameLike("Jo%").build();
		assertEquals(2, d.count(f));
		List<Person> results = d.filter(f);
		assertEquals(2, results.size());
		assertTrue(results.stream().allMatch(p -> p.getName().startsWith("Jo")));
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	@NoArgsConstructor
	@SuperBuilder
	static class BadManualFilter extends BaseFilter {
		// Invalid: @Attribute(manual=true) cannot combine with @From / @To / @WhenNull.
		@Attribute(value = "birthDay", manual = true) @From
		private java.util.Date birthDayFrom;
	}

	@Test
	void manualField_rejectsRangeAnnotations() {
		BadManualFilter bad = BadManualFilter.builder().build();
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> dao.count(bad));
		assertTrue(ex.getMessage().contains("manual=true"),
				"error message should mention manual=true; got: " + ex.getMessage());
	}

	@Test
	void criteriaHookCanMutateQuery() {
		// Validate the hook can run cq.* operations against the live query. Applying
		// distinct on a SELECT-entity query with unique IDs is a no-op result-wise
		// but a valid Criteria mutation that exercises the hook end-to-end.
		HookDao d = new HookDao(em) {
			@Override
			public void customizeCriteria(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
					Root<Person> root) {
				cq.distinct(true);
			}
		};
		List<Person> people = d.filter(PersonFilter.builder().build());
		assertEquals(4, people.size());
	}

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	void criteriaHookCannotReplaceSelection() {
		HookDao d = new HookDao(em) {
			@Override
			public void customizeCriteria(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
					Root<Person> root) {
				((CriteriaQuery) cq).select(root.get("name"));
			}
		};
		PersonFilter f = PersonFilter.builder().build();

		assertThrows(IllegalStateException.class, () -> d.filter(f));
		assertThrows(IllegalStateException.class, () -> d.filterWithSingleResult(f));
		assertThrows(IllegalStateException.class, () -> d.count(f));
	}

	@Test
	void criteriaHookCannotGroupCountQuery() {
		HookDao d = new HookDao(em) {
			@Override
			public void customizeCriteria(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
					Root<Person> root) {
				cq.groupBy(root.get("name"));
			}
		};
		assertThrows(IllegalStateException.class, () -> d.count(PersonFilter.builder().build()));
	}

	@Test
	void distinctFromCriteriaHook_countsDistinctEntities() {
		// A second root yields one row per (person, city) pair; distinct collapses
		// them back to the four people in both the list and the count query.
		EntityManager tx = emf.createEntityManager();
		tx.getTransaction().begin();
		for (String name : new String[] {"Rome", "Paris"}) {
			City city = new City();
			city.setName(name);
			tx.persist(city);
		}
		tx.getTransaction().commit();
		tx.close();

		HookDao d = new HookDao(em) {
			@Override
			public void customizeCriteria(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
					Root<Person> root) {
				cq.from(City.class);
				cq.distinct(true);
			}
		};
		PersonFilter f = PersonFilter.builder().build();

		assertEquals(4, d.filter(f).size());
		assertEquals(4, d.count(f));
	}
}
