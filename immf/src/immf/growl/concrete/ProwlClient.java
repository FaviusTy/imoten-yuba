/*
 * imoten - i mode.net mail tensou(forward)
 *
 * Copyright (C) 2011 StarAtlas (http://code.google.com/p/imoten/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package immf.growl.concrete;

import immf.Config;
import immf.growl.GrowlApiClient;

public class ProwlClient extends GrowlApiClient {

	//シングルトンインスタンス
	private static GrowlApiClient instance = new ProwlClient();

	//シングルトンインスタンスの取得
	public static GrowlApiClient getInstance() {
		return instance;
	}

	private ProwlClient() {

		super();

		this.topLevelTag = "prowl";

		this.verifyUrl = "https://api.prowlapp.com/publicapi/verify?apikey=";

		this.postUrl = "https://api.prowlapp.com/publicapi/add";
	}

	@Override
	public String getApiKeyFromConfig(Config config) {
		return config.getForwordProwlKeys();
	}

}
