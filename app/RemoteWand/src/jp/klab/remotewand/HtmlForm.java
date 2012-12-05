/*
* Copyright (C) 2012 KLab Inc.
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
*/
package jp.klab.remotewand;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;
import android.os.Environment;

public class HtmlForm {
	private final static String TAG = "RemoteWand";
	
	public static String fileName() {
		return Environment.getExternalStorageDirectory() + File.separator + "RemoteWand.html";		
	}
	
	public static boolean create(String id, Context ctx) {
		String htmlPath = fileName();
		PrintWriter pw = null;
		File file = new File(htmlPath);
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch (IOException e) {
			_Log.e(TAG, "HtmlForm open err: " + e.toString());
			return false;
		}
		pw.println("<DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>");
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<meta http-equiv='content-type' content='text/html; charset=Shift-JIS'>");
		pw.println("<title>RemoteWand</title></head>");
		pw.println("<body>");
		pw.println(ctx.getString(R.string.MsgHtmlForm));
		pw.println("<hr size=0>");
		pw.println("<p>");
		pw.println("<form name='form1' action='" + ctx.getString(R.string.ServerUrl) +"' method='POST'>");
		pw.println("<input type='hidden' name='id' value='" + id + "'>");
		pw.println("password <input type='password' name='pass' value=''>");
		pw.println("<input type='submit' value='PUSH'>");
		pw.println("</form>");
		pw.println("<hr size=0>");
		pw.println("</body>");
		pw.println("</html>");
		pw.close();
		return true;
	}
}
