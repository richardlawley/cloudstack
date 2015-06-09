// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.solidfire.dataaccess.vo;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.cloudstack.solidfire.dataaccess.SfVolume;

import com.cloud.utils.db.GenericDao;

@Entity
@Table(name = "sf_volume")
public class SfVolumeVO implements SfVolume {
    private static final long serialVersionUID = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "sf_id")
    private long sfId;

    @Column(name = "name")
    private String name;

    @Column(name = "size")
    private long size;

    @Column(name = "min_iops")
    private long minIops;

    @Column(name = "max_iops")
    private long maxIops;

    @Column(name = "burst_iops")
    private long burstIops;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "sf_cluster_id")
    private long sfClusterId;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date updated;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    public SfVolumeVO() {
        uuid = UUID.randomUUID().toString();
    }

    public SfVolumeVO(long sfId, String name, long size, long minIops, long maxIops, long burstIops, long accountId, long sfClusterId) {
        this.uuid = UUID.randomUUID().toString();
        this.sfId = sfId;
        this.name = name;
        this.size = size;
        this.minIops = minIops;
        this.maxIops = maxIops;
        this.burstIops = burstIops;
        this.accountId = accountId;
        this.sfClusterId = sfClusterId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public long getSfId() {
        return sfId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getMinIops() {
        return minIops;
    }

    @Override
    public long getMaxIops() {
        return maxIops;
    }

    @Override
    public long getBurstIops() {
        return burstIops;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getSfClusterId() {
        return sfClusterId;
    }
}
