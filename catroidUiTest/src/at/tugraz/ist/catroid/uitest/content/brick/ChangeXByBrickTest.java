/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.uitest.content.brick;

import java.util.ArrayList;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Smoke;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.content.Project;
import at.tugraz.ist.catroid.content.Script;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.StartScript;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.content.bricks.ChangeXByBrick;
import at.tugraz.ist.catroid.ui.ScriptActivity;
import at.tugraz.ist.catroid.uitest.util.UiTestUtils;
import at.tugraz.ist.catroid.utils.Utils;

import com.jayway.android.robotium.solo.Solo;

public class ChangeXByBrickTest extends ActivityInstrumentationTestCase2<ScriptActivity> {
	private Solo solo;
	private Project project;
	private ChangeXByBrick changeXByBrick;
	private int xToChange;

	public ChangeXByBrickTest() {
		super("at.tugraz.ist.catroid", ScriptActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		createProject();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		try {
			solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		getActivity().finish();
		super.tearDown();
	}

	@Smoke
	public void testChangeXByBrick() {
		int childrenCount = getActivity().getAdapter().getChildCountFromLastGroup();
		int groupCount = getActivity().getAdapter().getGroupCount();

		assertEquals("Incorrect number of bricks.", 2, solo.getCurrentListViews().get(0).getChildCount());
		assertEquals("Incorrect number of bricks.", 1, childrenCount);

		ArrayList<Brick> projectBrickList = project.getSpriteList().get(0).getScript(0).getBrickList();
		assertEquals("Incorrect number of bricks.", 1, projectBrickList.size());

		assertEquals("Wrong Brick instance.", projectBrickList.get(0),
				getActivity().getAdapter().getChild(groupCount - 1, 0));
		assertNotNull("TextView does not exist.", solo.getText(getActivity().getString(R.string.brick_change_x_by)));

		solo.clickOnEditText(0);
		solo.clearEditText(0);
		solo.enterText(0, xToChange + "");
		solo.goBack();
		solo.clickOnButton(0);

		solo.sleep(300);
		int xMovementValue = (Integer) UiTestUtils.getPrivateField("xMovement", changeXByBrick);
		assertEquals("Wrong text in field.", xToChange, xMovementValue);
		assertEquals("Value in Brick is not updated.", xToChange + "", solo.getEditText(0).getText().toString());
	}

	public void testResizeInputField() {
		int[] xTestValues = new int[] { 1, 123, 12345, -1, -12, -1000, -999 };
		int currentXValue = 0;
		int editTextWidth = 0;
		for (int i = 0; i < xTestValues.length; i++) {
			currentXValue = xTestValues[i];
			UiTestUtils.insertIntegerIntoEditText(solo, 0, currentXValue);
			solo.clickOnButton(0);
			solo.sleep(100);
			assertTrue("EditText not resized - value not (fully) visible", solo.searchText(currentXValue + ""));
			if ((currentXValue == 1) || (currentXValue == -1)) {
				editTextWidth = solo.getEditText(0).getWidth();
				assertTrue("Minwidth of EditText should be 50 dpi",
						editTextWidth >= Utils.getPhysicalPixels(50, solo.getCurrentActivity().getBaseContext()));
			}
		}

		solo.sleep(200);
		currentXValue = 123456;
		UiTestUtils.insertIntegerIntoEditText(solo, 0, currentXValue);
		solo.clickOnButton(0);
		solo.sleep(100);
		assertFalse("Number too long - should not be resized and fully visible", solo.searchText(currentXValue + ""));
	}

	private void createProject() {
		xToChange = 17;
		project = new Project(null, "testProject");
		Sprite sprite = new Sprite("cat");
		Script script = new StartScript(sprite);
		changeXByBrick = new ChangeXByBrick(sprite, 0);
		script.addBrick(changeXByBrick);

		sprite.addScript(script);
		project.addSprite(sprite);

		ProjectManager.getInstance().setProject(project);
		ProjectManager.getInstance().setCurrentSprite(sprite);
		ProjectManager.getInstance().setCurrentScript(script);
	}
}
