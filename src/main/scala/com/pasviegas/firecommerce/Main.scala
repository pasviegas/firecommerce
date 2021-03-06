// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.pasviegas.firecommerce

import com.pasviegas.firecommerce.domain.stock.SkuManager
import com.pasviegas.firecommerce.domain.users.UserManager
import com.pasviegas.firekka.Firekka
import com.pasviegas.firekka.actors.support.FirebaseActorCreator
import com.typesafe.config.ConfigFactory

object Main extends App {

  val userManagers = FirebaseActorCreator(UserManager.props)
  val skuManagers = FirebaseActorCreator(SkuManager.props)

  Firekka.system("firecommerce", ConfigFactory.load)
    .attachRoot("users", userManagers)
    .attachRoot("skus", skuManagers)
}
