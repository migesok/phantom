/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.dsl.crud

import scala.concurrent.blocking

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.datastax.driver.core.utils.UUIDs

import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Recipe, Recipes }

import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class ListOperatorsTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace = "listoperators"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Recipes.insertSchema()
    }
  }

  it should "store items in a list in the same order" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val list = List("test, test2, test3, test4, test5")

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, list)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list
      }
    }
  }

  it should "store items in a list in the same order with Twitter Futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val list = List("test, test2, test3, test4, test5")

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, list)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val operation = for {
      insertDone <- insert
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list
      }
    }
  }

  it should "store the same list size in Cassandra as it does in Scala" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val limit = 100
    val list = List.range(0, limit).map(_.toString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, list)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list
        items.get.size shouldEqual limit
      }
    }
  }

  it should "store the same list size in Cassandra as it does in Scala with Twitter Futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val limit = 100
    val list = List.range(0, limit).map(_.toString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, list)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list
        items.get.size shouldEqual limit
      }
    }
  }

  it should "append an item to a list" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append "test").future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: List("test")
      }
    }
  }

  it should "append an item to a list with Twitter futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append "test").execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: List("test")
      }
    }
  }

  it should "append several items to a list" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val appendable = List("test", "test2")

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients appendAll appendable).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: appendable
      }
    }
  }

  it should "append several items to a list with Twitter futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val appendable = List("test", "test2")

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients appendAll appendable).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: appendable
      }
    }
  }

  it should "prepend an item to a list" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend "test").future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List("test") :::  recipe.ingredients
      }
    }
  }

  it should "prepend an item to a list with Twitter Futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend "test").execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List("test") :::  recipe.ingredients
      }
    }
  }

  it should "prepend several items to a list" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val appendable = List("test", "test2")
    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prependAll appendable).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual appendable.reverse ::: recipe.ingredients
      }
    }
  }

  it should "prepend several items to a list with Twitter futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val appendable = List("test", "test2")
    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prependAll appendable).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual appendable.reverse ::: recipe.ingredients
      }
    }
  }

  it should "remove an item from a list" in {
    val list = List("test, test2")
    val recipe = Recipe.sample.copy(ingredients = list)
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.head).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list.tail
      }
    }
  }

  it should "remove an item from a list with Twitter Futures" in {
    val list = List("test, test2")
    val recipe = Recipe.sample.copy(ingredients = list)
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.head).execute
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list.tail
      }
    }
  }

  it should "remove multiple items from a list" in {
    val list = List("test, test2, test3, test4, test5")
    val recipe = Recipe.sample.copy(ingredients = list)
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discardAll list.tail).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List(list.head)
      }
    }
  }

  it should "remove multiple items from a list with Twitter futures" in {
    val list = List("test, test2, test3, test4, test5")
    val recipe = Recipe.sample.copy(ingredients = list)
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discardAll list.tail).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List(list.head)
      }
    }
  }

  it should "set a 0 index inside a List" in {
    val list = List("test, test2, test3, test4, test5")
    val recipe = Recipe.sample.copy(ingredients = list)
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (0, "updated")).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(0) shouldEqual "updated"
      }
    }
  }

  it should "set an index inside a List with Twitter futures" in {
    val list = List("test, test2, test3, test4, test5")
    val recipe = Recipe.sample.copy(ingredients = list)
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (0, "updated")).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(0) shouldEqual "updated"
      }
    }
  }

  it should "set the third index inside a List" in {
    val list = List.range(0, 100).map(_.toString)
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, list)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (3, "updated")).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(3) shouldEqual "updated"
      }
    }
  }

  it should "set the third index inside a List with Twitter Futures" in {
    val list = List.range(0, 100).map(_.toString)
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, list)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (3, "updated")).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(3) shouldEqual "updated"
      }
    }
  }
}
