/*
 * Copyright 2013 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import org.specs2.mutable.Specification

class PagerSpec extends Specification {

  "Pager#adjust" should {
    "データ件数0ならば、p.0->p.0、p.1->p.0、p.2->p.0" in {
      Pager(Some(0L), 5L).adjust(0L) must equalTo(Pager(Some(0L), 5L))
      Pager(Some(1L), 5L).adjust(0L) must equalTo(Pager(Some(0L), 5L))
      Pager(Some(2L), 5L).adjust(0L) must equalTo(Pager(Some(0L), 5L))
    }
    "データ件数5 (ページサイズと同じ) ならば、p.0->p.0、p.1->p.0、p.2->p.0" in {
      Pager(Some(0L), 5L).adjust(5L) must equalTo(Pager(Some(0L), 5L))
      Pager(Some(1L), 5L).adjust(5L) must equalTo(Pager(Some(0L), 5L))
      Pager(Some(2L), 5L).adjust(5L) must equalTo(Pager(Some(0L), 5L))
    }
    "データ件数6 (ページサイズ+1) ならば、p.0->p.0、p.1->p.1、p.2->p.1" in {
      Pager(Some(0L), 5L).adjust(6L) must equalTo(Pager(Some(0L), 5L))
      Pager(Some(1L), 5L).adjust(6L) must equalTo(Pager(Some(1L), 5L))
      Pager(Some(2L), 5L).adjust(6L) must equalTo(Pager(Some(1L), 5L))
    }
    "データ件数10 (ページサイズ*2) ならば、p.0->p.0、p.1->p.1、p.2->p.1" in {
      Pager(Some(0L), 5L).adjust(10L) must equalTo(Pager(Some(0L), 5L))
      Pager(Some(1L), 5L).adjust(10L) must equalTo(Pager(Some(1L), 5L))
      Pager(Some(2L), 5L).adjust(10L) must equalTo(Pager(Some(1L), 5L))
    }
    "データ件数11 (ページサイズ*2+1) ならば、p.0->p.0、p.1->p.1、p.2->p.1" in {
      Pager(Some(0L), 5L).adjust(11L) must equalTo(Pager(Some(0L), 5L))
      Pager(Some(1L), 5L).adjust(11L) must equalTo(Pager(Some(1L), 5L))
      Pager(Some(2L), 5L).adjust(11L) must equalTo(Pager(Some(2L), 5L))
    }
  }

}
