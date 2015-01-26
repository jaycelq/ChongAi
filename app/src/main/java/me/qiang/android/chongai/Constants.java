/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package me.qiang.android.chongai;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class Constants {

    public static final String ALLIMAGE = "所有图片";

	private Constants() {
	}

	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}
	
	public static class Extra {
		public static final String FRAGMENT_INDEX = "com.nostra13.example.universalimageloader.FRAGMENT_INDEX";
		public static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
        public static final String IMAGE_TO_SHOW = "me.qiang.android.imagepager.IMAGE_TO_SHOW";
        public static final String IMAGE_SELECTED = "me.qiang.android.imagegrid.IMAGE_SELECTED";
        public static final String BITMAP_SELECTED = "me.qiang.android.imagegrid.BITMAP_SELECTED";

        public static final int ALBUM_MUTIPLE_SELECT = 100;
	}

    public static class Login {
        public static final int NO_USER_PROFILE = 4001;
        public static final int NO_SUCH_USER = 4002;
        public static final int WRONG_PASSWORD = 4003;
    }

    public static class Image {
        public static final int PICK_IMAGE = 1001;
        public static final int TAKE_PHOTO = 1002;

        public static final String IMAGE_RESULT = "image_url";
    }

    public static class FragmentTag {
        public static final String STATE_FRAGMENT =
                "me.qiang.android.fragment.StateFragment";
        public static final String DRAWER_FRAGMENT =
                "me.qiang.android.fragment.DrawerFragment";
        public static final String BOTTOM_NAVIGATION_FRAGMENT =
                "me.qiang.android.fragment.BottomNavigationFragment";
        public static final String CREATE_NEW_STATE_FRAGMENT =
                "me.qiang.android.fragment.CreateNewStateFragment";
        public static final String BOTTOM_POPUP_FRAGMENT =
                "me.qiang.android.fragment.BottomPopUpFragment";
    }

    public static class StateManager {
        public static final String STATE_INDEX = "state_index";
    }

    public static class StateItem {
        public static final String STATE_ID = "state_index";
        public static final String LAST_PRAISE_USER = "last_praise_user";
    }
}
