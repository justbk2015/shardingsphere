/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.metadata.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * ShardingSphere user.
 */
@Getter
@EqualsAndHashCode(of = "grantee")
public final class ShardingSphereUser {
    
    private final Grantee grantee;
    
    private final String password;
    
    private final String authType;
    
    public ShardingSphereUser(final String username, final String password, final String hostname) {
        this(username, password, hostname, null);
    }
    
    public ShardingSphereUser(final String username, final String password, final String hostname, final String authType) {
        grantee = new Grantee(username, hostname);
        this.password = password;
        this.authType = authType;
    }
}
