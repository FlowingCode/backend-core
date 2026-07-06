/*-
 * #%L
 * Commons Backend - Model
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
package com.flowingcode.backendcore.model.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Arrays;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import org.junit.jupiter.api.Test;

class BaseFilterTest {

	@Getter
	@Setter
	@Accessors(chain = true)
	@NoArgsConstructor
	@SuperBuilder(toBuilder = true)
	static class SampleFilter extends BaseFilter {

		@Attribute("name")
		private String name;

		@Attribute("birthDate") @From
		private LocalDate birthDateFrom;

		@Attribute("birthDate") @To(inclusive = false)
		private LocalDate birthDateTo;
	}

	@Test
	void pojo_chainableSetters_carryAllState() {
		SampleFilter f = new SampleFilter()
				.setName("Ada")
				.setBirthDateFrom(LocalDate.of(1990, 1, 1))
				.setBirthDateTo(LocalDate.of(2000, 1, 1));
		f.addOrder("name").setMaxResult(50).setFirstResult(10);

		assertEquals("Ada", f.getName());
		assertEquals(LocalDate.of(1990, 1, 1), f.getBirthDateFrom());
		assertEquals(LocalDate.of(2000, 1, 1), f.getBirthDateTo());
		assertEquals(50, f.getMaxResult());
		assertEquals(10, f.getFirstResult());
		assertIterableEquals(Arrays.asList("name"), f.getOrders().keySet());
		assertEquals(BaseFilter.Order.ASC, f.getOrders().get("name"));
	}

	@Test
	void builder_setsState() {
		SampleFilter f = SampleFilter.builder()
				.name("Ada")
				.birthDateFrom(LocalDate.of(1990, 1, 1))
				.addOrder("name", BaseFilter.Order.DESC)
				.addOrder("birthDate", BaseFilter.Order.ASC)
				.firstResult(0)
				.maxResult(25)
				.build();

		assertEquals("Ada", f.getName());
		assertEquals(LocalDate.of(1990, 1, 1), f.getBirthDateFrom());
		assertNull(f.getBirthDateTo());
		assertEquals(0, f.getFirstResult());
		assertEquals(25, f.getMaxResult());
		assertIterableEquals(Arrays.asList("name", "birthDate"), f.getOrders().keySet());
		assertEquals(BaseFilter.Order.DESC, f.getOrders().get("name"));
		assertEquals(BaseFilter.Order.ASC, f.getOrders().get("birthDate"));
	}

	@Test
	void orders_preserveInsertionOrder() {
		SampleFilter f = new SampleFilter();
		f.addOrder("c").addOrder("a").addOrder("b", BaseFilter.Order.DESC);

		assertIterableEquals(Arrays.asList("c", "a", "b"), f.getOrders().keySet());
		assertEquals(BaseFilter.Order.ASC, f.getOrders().get("c"));
		assertEquals(BaseFilter.Order.DESC, f.getOrders().get("b"));
	}

	@Test
	void emptyFilter_hasEmptyOrders_andNullPaging() {
		SampleFilter f = new SampleFilter();
		assertEquals(0, f.getOrders().size());
		assertNull(f.getFirstResult());
		assertNull(f.getMaxResult());
	}

	@Test
	void firstResult_rejectsNegativeFromPojo() {
		SampleFilter f = new SampleFilter();
		assertThrows(IllegalArgumentException.class, () -> f.setFirstResult(-1));
	}

	@Test
	void maxResult_rejectsNegativeFromPojo() {
		SampleFilter f = new SampleFilter();
		assertThrows(IllegalArgumentException.class, () -> f.setMaxResult(-1));
	}

	@Test
	void firstResult_rejectsNegativeFromBuilder() {
		assertThrows(IllegalArgumentException.class,
				() -> SampleFilter.builder().firstResult(-1).build());
	}

	@Test
	void maxResult_rejectsNegativeFromBuilder() {
		assertThrows(IllegalArgumentException.class,
				() -> SampleFilter.builder().maxResult(-1).build());
	}

	@Test
	void toBuilder_producesIndependentCopy() {
		SampleFilter original = SampleFilter.builder().name("Ada").maxResult(50).build();
		SampleFilter tweaked = original.toBuilder().maxResult(10).build();

		assertEquals(50, original.getMaxResult());
		assertEquals(10, tweaked.getMaxResult());
		assertEquals("Ada", tweaked.getName());
		assertNotSame(original, tweaked);
	}

	@Test
	void toBuilder_ordersAreIsolatedFromOriginal() {
		SampleFilter original = SampleFilter.builder().addOrder("a").build();
		SampleFilter tweaked = original.toBuilder()
				.addOrder("b", BaseFilter.Order.DESC)
				.build();

		assertIterableEquals(Arrays.asList("a"), original.getOrders().keySet(),
				"adding an order on the clone builder must not mutate the original");
		assertIterableEquals(Arrays.asList("a", "b"), tweaked.getOrders().keySet());
		assertEquals(BaseFilter.Order.DESC, tweaked.getOrders().get("b"));
	}
}
