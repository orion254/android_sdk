/**
 * Copyright (c) 2015-2016 Tapglue (https://www.tapglue.com/). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tapglue.tapgluesdk;

import android.content.Context;

import com.tapglue.tapgluesdk.entities.User;
import com.tapglue.tapgluesdk.http.ServiceFactory;

import rx.Observable;

public class Tapglue {

    Configuration configuration;
    Network network;
    UserStore currentUser;

    public Tapglue(Configuration configuration, Context context) {
        this.configuration = configuration;
        this.network = new Network(new ServiceFactory(configuration));
        this.currentUser = new UserStore(context.getSharedPreferences("currentUser", Context.MODE_PRIVATE));
    }

    public Observable<User> loginWithUsername(String username, String password) {
        return network.loginWithUsername(username, password).map(currentUser.store());
    }

    public Observable<User> loginWithEmail(String email, String password) {
        return network.loginWithEmail(email, password).map(currentUser.store());
    }

    public Observable<Void> logout() {
        return network.logout().doOnCompleted(currentUser.clear());
    }

    public Observable<User> getCurrentUser() {
        return currentUser.get();
    }
}
