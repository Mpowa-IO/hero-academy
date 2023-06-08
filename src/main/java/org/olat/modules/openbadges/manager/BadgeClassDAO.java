/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openbadges.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.model.BadgeClassImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-05-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BadgeClassDAO {

	@Autowired
	private DB dbInstance;

	public BadgeClass createBadgeClass(String uuid, String version, String image, String name, String description,
									   String criteria, String issuer) {
		BadgeClassImpl badgeClass = new BadgeClassImpl();
		badgeClass.setCreationDate(new Date());
		badgeClass.setLastModified(badgeClass.getCreationDate());
		badgeClass.setUuid(uuid);
		badgeClass.setStatus(BadgeClass.BadgeClassStatus.active);
		badgeClass.setVersion(version);
		badgeClass.setImage(image);
		badgeClass.setName(name);
		badgeClass.setDescription(description);
		badgeClass.setCriteria(criteria);
		badgeClass.setIssuer(issuer);
		dbInstance.getCurrentEntityManager().persist(badgeClass);
		return badgeClass;
	}

	public List<BadgeClass> getBadgeClasses() {
		String q = "select class from badgeclass class order by class.name asc";
		return dbInstance.getCurrentEntityManager().createQuery(q, BadgeClass.class).getResultList();
	}

	public BadgeClass getBadgeClass(String uuid) {
		String query = "select bc from badgeclass bc where bc.uuid=:uuid";
		List<BadgeClass> badgeClasses = dbInstance.getCurrentEntityManager()
				.createQuery(query, BadgeClass.class)
				.setParameter("uuid", uuid)
				.getResultList();
		return badgeClasses == null || badgeClasses.isEmpty() ? null : badgeClasses.get(0);
	}

	public BadgeClass updateBadgeClass(BadgeClass badgeClass) {
		badgeClass.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(badgeClass);
	}

	public void deleteBadgeClass(BadgeClass badgeClass) {
		dbInstance.deleteObject(badgeClass);
	}
}