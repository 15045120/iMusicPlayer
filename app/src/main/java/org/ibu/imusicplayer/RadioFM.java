/**
 * Copyright 2019 Ibu
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
package org.ibu.imusicplayer;

import java.io.Serializable;

/**
 * 电台实体类
 */
public class RadioFM implements Serializable {
    private String contentId;       //电台ID
    private String title;           //电台名
    private String cover;           //电台封面
    private String description;     //描述
    /**
     * 同一个id的电台视为同一电台
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        return contentId.equalsIgnoreCase(((RadioFM)obj).contentId);
    }

    @Override
    public String toString() {
        return title+"@"+description;
    }

    public RadioFM(String contentId, String title, String cover, String description) {
        this.contentId = contentId;
        this.title = title;
        this.cover = cover;
        this.description = description;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
