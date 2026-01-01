/*
 * Copyright (C) 2021 - 2025 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limboauth.dependencies.crafter.model;

/**
 * Model class for Crafter CMS website information.
 */
public class CrafterWebsite {
  private final String id;
  private final String name;
  private final String url;
  private String licenseExpiresAt;

  public CrafterWebsite(String id, String name, String url) {
    this.id = id;
    this.name = name;
    this.url = url;
  }

  public CrafterWebsite(String id, String name, String url, String licenseExpiresAt) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.licenseExpiresAt = licenseExpiresAt;
  }

  /**
   * Get the website ID.
   *
   * @return The website ID
   */
  public String getId() {
    return this.id;
  }

  /**
   * Get the website name.
   *
   * @return The website name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the website URL.
   *
   * @return The website URL
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * Get the license expiration date.
   *
   * @return The license expiration date (ISO 8601 format)
   */
  public String getLicenseExpiresAt() {
    return this.licenseExpiresAt;
  }

  /**
   * Set the license expiration date.
   *
   * @param licenseExpiresAt The license expiration date
   */
  public void setLicenseExpiresAt(String licenseExpiresAt) {
    this.licenseExpiresAt = licenseExpiresAt;
  }

  @Override
  public String toString() {
    return "CrafterWebsite{"
        + "id='" + this.id + '\''
        + ", name='" + this.name + '\''
        + ", url='" + this.url + '\''
        + ", licenseExpiresAt='" + this.licenseExpiresAt + '\''
        + ", hashCode=" + System.identityHashCode(this)
        + '}';
  }
}
