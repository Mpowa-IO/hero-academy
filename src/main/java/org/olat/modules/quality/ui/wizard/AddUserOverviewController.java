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
package org.olat.modules.quality.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.quality.ui.ParticipationListController;
import org.olat.modules.quality.ui.QualityMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class AddUserOverviewController extends StepFormBasicController {

	protected static final String USER_PROPS_ID = QualityMainController.class.getCanonicalName();
	
	private List<Identity> oks;
	private List<String> notfounds;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;

	public AddUserOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(ParticipationListController.class, getLocale(), getTranslator()));

		oks = null;
		if(containsRunContextKey("keys")) {
			@SuppressWarnings("unchecked")
			List<String> keys = (List<String>)runContext.get("keys");
			loadModel(keys);
		}

		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_user_import_overview");
		
		//add the table
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		int colIndex = 0;
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, false);
		List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<>();
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			if(visible) {
				resultingPropertyHandlers.add(userPropertyHandler);
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++));
			}
		}
		
		Translator myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
		AddUserOverviewDataModel userTableModel = new AddUserOverviewDataModel(oks, resultingPropertyHandlers,
				getLocale(), tableColumnModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "users", userTableModel, myTrans, formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadModel(List<String> keys) {
		oks = new ArrayList<>();
		List<String> isanonymous = new ArrayList<>();
		notfounds = new ArrayList<>();

		for (String identityKey : keys) {
			Identity ident = securityManager.loadIdentityByKey(Long.parseLong(identityKey));
			if (ident == null) { // not found, add to not-found-list
				notfounds.add(identityKey);
			} else if (organisationService.hasRole(ident, OrganisationRoles.guest)) {
				isanonymous.add(identityKey);
			} else if (!PersistenceHelper.containsPersistable(oks, ident)) {
				oks.add(ident);
			}
		}
	}
	
	public boolean validate() {
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		IdentityContext identityContext = (IdentityContext) getFromRunContext("identityContext");
		identityContext.setIdentities(oks);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}
}