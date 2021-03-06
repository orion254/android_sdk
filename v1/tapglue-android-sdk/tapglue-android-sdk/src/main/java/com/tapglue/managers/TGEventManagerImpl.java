/*
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

package com.tapglue.managers;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tapglue.Tapglue;
import com.tapglue.model.TGEvent;
import com.tapglue.model.TGEventObject;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

public class TGEventManagerImpl extends AbstractTGManager implements TGEventManager {

    public TGEventManagerImpl(Tapglue instance) {
        super(instance);
    }

    @Override
    public void createEvent(@NonNull String type, @NonNull final TGRequestCallback<TGEvent> callback) {
        if (TextUtils.isEmpty(type)) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.NULL_INPUT));
            return;
        }
        else if (instance.getUserManager().getCurrentUser() == null) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.USER_NOT_LOGGED_IN));
            return;
        }
        TGEvent event = new TGEvent(instance).setType(type);
        instance.createRequest().createEvent(event, callback);
    }

    @Override
    public void createEvent(@NonNull String type, @NonNull TGEventObject object, @NonNull final TGRequestCallback<TGEvent> callback) {
        if (TextUtils.isEmpty(type)) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.NULL_INPUT));
            return;
        }
        else if (instance.getUserManager().getCurrentUser() == null) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.USER_NOT_LOGGED_IN));
            return;
        }
        TGEvent event = new TGEvent(instance).setType(type).setObject(object);
        instance.createRequest().createEvent(event, callback);
    }

    @Override
    public void createEvent(@NonNull String type, @NonNull String objectId, @NonNull final TGRequestCallback<TGEvent> callback) {
        if (TextUtils.isEmpty(type)) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.NULL_INPUT));
            return;
        }
        else if (instance.getUserManager().getCurrentUser() == null) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.USER_NOT_LOGGED_IN));
            return;
        }
        TGEvent event = new TGEvent(instance).setType(type).setObject(new TGEventObject().setID(objectId));
        instance.createRequest().createEvent(event, callback);
    }

    @Override
    public void createEvent(@NonNull TGEvent event, @NonNull final TGRequestCallback<TGEvent> callback) {
        if (instance.getUserManager().getCurrentUser() == null) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.USER_NOT_LOGGED_IN));
            return;
        }
        instance.createRequest().createEvent(new TGEvent(instance, event), callback);
    }

    @Override
    public void removeEvent(@NonNull Long id, @NonNull final TGRequestCallback<Object> callback) {
        if (instance.getUserManager().getCurrentUser() == null) {
            callback.onRequestError(new TGRequestErrorType(TGRequestErrorType.ErrorType.USER_NOT_LOGGED_IN));
            return;
        }
        instance.createRequest().removeEvent(id, callback);
    }
}
