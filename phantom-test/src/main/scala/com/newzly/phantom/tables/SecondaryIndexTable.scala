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
package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.helper.{ ModelSampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.util.testing.Sampler


case class SecondaryIndexRecord(primary: UUID, secondary: UUID, name: String)

object SecondaryIndexRecord extends ModelSampler[SecondaryIndexRecord] {
  def sample: SecondaryIndexRecord = SecondaryIndexRecord(
    UUIDs.timeBased(),
    UUIDs.timeBased(),
    Sampler.getARandomString
  )
}

sealed class SecondaryIndexTable extends CassandraTable[SecondaryIndexTable, SecondaryIndexRecord] {
  
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object secondary extends UUIDColumn(this) with Index[UUID]
  object name extends StringColumn(this)
  
  def fromRow(r: Row): SecondaryIndexRecord = SecondaryIndexRecord(
    id(r),
    secondary(r),
    name(r)
  )
}

object SecondaryIndexTable extends SecondaryIndexTable with TestSampler[SecondaryIndexTable, SecondaryIndexRecord]
