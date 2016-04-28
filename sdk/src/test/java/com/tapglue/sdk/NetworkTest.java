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
package com.tapglue.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.tapglue.sdk.entities.Connection;
import com.tapglue.sdk.entities.ConnectionList;
import com.tapglue.sdk.entities.Follow;
import com.tapglue.sdk.entities.User;
import com.tapglue.sdk.http.UsersFeed;
import com.tapglue.sdk.http.ConnectionsFeed;
import com.tapglue.sdk.http.ServiceFactory;
import com.tapglue.sdk.http.TapglueService;
import com.tapglue.sdk.http.payloads.EmailLoginPayload;
import com.tapglue.sdk.http.payloads.UsernameLoginPayload;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.Every.everyItem;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Network.class)
public class NetworkTest {
    private static final String EMAIL = "user@domain.com";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";


    @Mock
    Context context;
    @Mock
    SharedPreferences prefs;
    @Mock
    ServiceFactory serviceFactory;
    @Mock
    TapglueService service;
    @Mock
    TapglueService secondService;
    @Mock
    SessionStore sessionStore;
    @Mock
    UUIDStore uuidStore;
    @Mock
    Func1<User, User> storeFunc;
    @Mock
    Store<String> internalUUIDStore;
    @Mock
    Action0 clearAction;
    @Mock
    UsersFeed usersFeed;
    @Mock
    User user;
    @Mock
    List<User> users;

    //SUT
    Network network;

    @Before
    public void setUp() throws Exception {
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
        when(service.login(isA(UsernameLoginPayload.class))).thenReturn(Observable.just(user));
        when(service.login(isA(EmailLoginPayload.class))).thenReturn(Observable.just(user));
        when(serviceFactory.createTapglueService()).thenReturn(service)
        .thenReturn(secondService);

        whenNew(UUIDStore.class).withAnyArguments().thenReturn(uuidStore);
        whenNew(SessionStore.class).withAnyArguments().thenReturn(sessionStore);
        when(uuidStore.get()).thenReturn(Observable.<String>empty());

        when(sessionStore.get()).thenReturn(Observable.<User>empty());
        when(sessionStore.store()).thenReturn(storeFunc);
        when(storeFunc.call(user)).thenReturn(user);
        when(sessionStore.clear()).thenReturn(clearAction);
        network = new Network(serviceFactory, context);
    }

    @Test
    public void loginWithUsernameReturnsUserFromService() {
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.loginWithUsername(USERNAME, PASSWORD).subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(user));
    }

    @Test
    public void loginWithEmailReturnsUserFromService() {
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.loginWithEmail(EMAIL, PASSWORD).subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(user));
    }


    @Test
    public void usernameLoginSetsSessionTokenToServiceFactory() {
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.loginWithUsername(USERNAME, PASSWORD).subscribe(ts);

        verify(serviceFactory).setSessionToken("sessionToken");
    }

    @Test
    public void emailLoginSetsSessionTokenToServiceFactory() {
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.loginWithEmail(EMAIL, PASSWORD).subscribe(ts);

        verify(serviceFactory).setSessionToken("sessionToken");
    }

    @Test
    public void usernameLoginStoresSessionToken() {
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.loginWithUsername(USERNAME, PASSWORD).subscribe(ts);

        verify(storeFunc).call(user);
    }

    @Test
    public void emailLoginStoresSessionToken() {
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.loginWithEmail(EMAIL, PASSWORD).subscribe(ts);

        verify(storeFunc).call(user);
    }

    @Test
    public void usernameLoginCreatesNewService() {
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.loginWithUsername(USERNAME, PASSWORD).subscribe(ts);

        assertThat(network.service, equalTo(secondService));
    }

    @Test
    public void logoutReturnsObservableFromNetwork() {
        when(service.logout()).thenReturn(Observable.<Void>empty());
        TestSubscriber<Void> ts = new TestSubscriber<>();

        network.logout().subscribe(ts);

        ts.assertNoErrors();
        ts.assertCompleted();
    }

    @Test
    public void logoutClearsSession() {
        when(service.logout()).thenReturn(Observable.<Void>empty());
        TestSubscriber<Void> ts = new TestSubscriber<>();

        network.logout().subscribe(ts);

        verify(clearAction).call();
    }

    @Test
    public void createUserReturnsUserFromService() {
        when(service.createUser(user)).thenReturn(Observable.just(user));
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.createUser(user).subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(user));
    }

    @Test
    public void deleteCurrentUserReturnsObservableFromService() {
        when(service.deleteCurrentUser()).thenReturn(Observable.<Void>empty());
        TestSubscriber<Void> ts = new TestSubscriber<>();

        network.deleteCurrentUser().subscribe(ts);

        ts.assertNoErrors();
        ts.assertCompleted();
    }

    @Test
    public void deleteCurrentUserClearsSession() {
        when(service.deleteCurrentUser()).thenReturn(Observable.<Void>empty());
        TestSubscriber<Void> ts = new TestSubscriber<>();

        network.deleteCurrentUser().subscribe(ts);

        verify(clearAction).call();
    }

    @Test
    public void updateCurrentUserReturnsUserFromService() {
        when(service.updateCurrentUser(user)).thenReturn(Observable.just(user));
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.updateCurrentUser(user).subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(user));
    }

    @Test
    public void updateCurrentUserStoresSessionToken() {
        when(service.updateCurrentUser(user)).thenReturn(Observable.just(user));
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.updateCurrentUser(user).subscribe(ts);

        verify(storeFunc).call(user);
    }

    @Test
    public void updateCurrentUserCreatesNewService() {
        when(service.updateCurrentUser(user)).thenReturn(Observable.just(user));
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.updateCurrentUser(user).subscribe(ts);

        verify(serviceFactory).setSessionToken("sessionToken");
    }

    @Test
    public void updateCurrentUserSetsSessionTokenToServiceFactory() {
        when(service.updateCurrentUser(user)).thenReturn(Observable.just(user));
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.updateCurrentUser(user).subscribe(ts);

        assertThat(network.service, equalTo(secondService));
    }

    @Test
    public void refreshCurrentUserReturnsUserFromService() {
        when(service.refreshCurrentUser()).thenReturn(Observable.just(user));
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.refreshCurrentUser().subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(user));
    }

    @Test
    public void refreshCurrentUserStoresSessionToken() {
        when(service.refreshCurrentUser()).thenReturn(Observable.just(user));
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.refreshCurrentUser().subscribe(ts);

        verify(storeFunc).call(user);
    }

    @Test
    public void refreshCurrentUserCreatesNewService() {
        when(service.refreshCurrentUser()).thenReturn(Observable.just(user));
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.refreshCurrentUser().subscribe(ts);

        verify(serviceFactory).setSessionToken("sessionToken");
    }

    @Test
    public void refreshCurrentUserSetsSessionTokenToServiceFactory() {
        when(service.refreshCurrentUser()).thenReturn(Observable.just(user));
        when(user.getSessionToken()).thenReturn("sessionToken");
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.refreshCurrentUser().subscribe(ts);

        assertThat(network.service, equalTo(secondService));
    }

    @Test
    public void retrieveUserReturnsUserFromService() {
        String id = "someID";
        when(service.retrieveUser(id)).thenReturn(Observable.just(user));
        TestSubscriber<User> ts = new TestSubscriber<>();

        network.retrieveUser(id).subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(user));
    }

    @Test
    public void retrieveFollowignsReturnsUsersFromService() {
        when(usersFeed.getUsers()).thenReturn(users);
        when(service.retrieveFollowings()).thenReturn(Observable.just(usersFeed));
        TestSubscriber<List<User>> ts = new TestSubscriber<>();

        network.retrieveFollowings().subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(users));
    }

    @Test
    public void retrieveFollowersReturnsUsersFromService() {
        when(usersFeed.getUsers()).thenReturn(users);
        when(service.retrieveFollowers()).thenReturn(Observable.just(usersFeed));
        TestSubscriber<List<User>> ts = new TestSubscriber<>();

        network.retrieveFollowers().subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(users));
    }

    @Test
    public void retrieveFriendsReturnsUsersFromService() {
        when(usersFeed.getUsers()).thenReturn(users);
        when(service.retrieveFriends()).thenReturn(Observable.just(usersFeed));
        TestSubscriber<List<User>> ts = new TestSubscriber<>();

        network.retrieveFriends().subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(users));
    }

    @Test
    public void userExtractorReplacesNullWithEmptyList() {
        when(usersFeed.getUsers()).thenReturn(null);
        when(service.retrieveFriends()).thenReturn(Observable.just(usersFeed));
        TestSubscriber<List<User>> ts = new TestSubscriber<>();

        network.retrieveFriends().subscribe(ts);

        assertThat(ts.getOnNextEvents().get(0), notNullValue());
    }

    @Test
    public void userExtractorReplacesNullFeedWithEmptyList() {
        when(service.retrieveFriends()).thenReturn(Observable.<UsersFeed>just(null));
        TestSubscriber<List<User>> ts = new TestSubscriber<>();

        network.retrieveFriends().subscribe(ts);

        assertThat(ts.getOnNextEvents().get(0), notNullValue());
    }

    @Test
    public void createConnectionReturnsConnectionFromService() {
        Connection connection = mock(Connection.class);
        when(service.createConnection(connection)).thenReturn(Observable.just(connection));
        TestSubscriber<Connection> ts = new TestSubscriber<>();

        network.createConnection(connection).subscribe(ts);

        assertThat(ts.getOnNextEvents(), hasItems(connection));
    }

    @Test
    public void retrievePendingConnectionsWhenNullReturnsEmptyLists() {
        when(service.retrievePendingConnections()).thenReturn(Observable.<ConnectionsFeed>just(null));
        TestSubscriber<ConnectionList> ts = new TestSubscriber<>();

        network.retrievePendingConnections().subscribe(ts);
        
        assertThat(ts.getOnNextEvents(), everyItem(notNullValue(ConnectionList.class)));
    }

    @Test
    public void retrievePendingConnectionsReturnsIncommingConnectionsFromService() {
        List<Connection> incomingConnections = new ArrayList<>();
        ConnectionsFeed feed = new ConnectionsFeed();
        feed.incoming = incomingConnections;
        feed.outgoing = new ArrayList<Connection>();
        feed.users = new ArrayList<User>();
        when(service.retrievePendingConnections()).thenReturn(Observable.just(feed));
        TestSubscriber<ConnectionList> ts = new TestSubscriber<>();

        network.retrievePendingConnections().subscribe(ts);

        ConnectionList connectionList = ts.getOnNextEvents().get(0);
        assertThat(connectionList.getIncomingConnections(), equalTo(incomingConnections));
    }

    @Test
    public void retrievePendingConectionsPopulatesUsersToIncoming() {
        Connection connection = mock(Connection.class);
        when(connection.getUserFromId()).thenReturn("id");
        User userFrom = mock(User.class);
        when(userFrom.getId()).thenReturn("id");
        ConnectionsFeed feed = new ConnectionsFeed();

        feed.users = Arrays.asList(userFrom);
        feed.incoming =  Arrays.asList(connection);
        when(service.retrievePendingConnections()).thenReturn(Observable.just(feed));
        TestSubscriber<ConnectionList> ts = new TestSubscriber<>();

        network.retrievePendingConnections().subscribe(ts);

        verify(connection).setUserFrom(userFrom);
    }

    @Test
    public void retrievePendingConectionsPopulatesUsersToOutgoing() {
        Connection connection = mock(Connection.class);
        when(connection.getUserToId()).thenReturn("id");
        User userTo = mock(User.class);
        when(userTo.getId()).thenReturn("id");
        ConnectionsFeed feed = new ConnectionsFeed();

        feed.users = Arrays.asList(userTo);
        feed.outgoing =  Arrays.asList(connection);
        feed.incoming = new ArrayList<Connection>();
        when(service.retrievePendingConnections()).thenReturn(Observable.just(feed));
        TestSubscriber<ConnectionList> ts = new TestSubscriber<>();

        network.retrievePendingConnections().subscribe(ts);

        verify(connection).setUserTo(userTo);
    }
    @Test
    public void sendAnalyticsCallsService() {
        when(service.sendAnalytics()).thenReturn(Observable.<Void>empty());
        TestSubscriber<Void> ts = new TestSubscriber<>();

        network.sendAnalytics().subscribe(ts);

        ts.assertNoErrors();
        ts.assertCompleted();
    }
}