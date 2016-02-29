/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.managedvms.gettingstartedjava.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.plus.PlusScopes;

import com.example.managedvms.gettingstartedjava.util.DatastoreHttpServlet;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@WebServlet(name = "login", value = "/login")
@SuppressWarnings("serial")
public class LoginServlet extends DatastoreHttpServlet {

  private GoogleAuthorizationCodeFlow flow;
  private Logger logger = Logger.getLogger(this.getClass().getName());
  private static final Collection<String> SCOPE =
      Arrays.asList(PlusScopes.USERINFO_EMAIL, PlusScopes.PLUS_LOGIN);
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // Create a unique identifier and store it in a cookie
    String sessionNum = new BigInteger(130, new SecureRandom()).toString(32);
    Cookie session = new Cookie("bookshelfSessionId", sessionNum);
    session.setPath("/");
    resp.addCookie(session);

    flow =
        new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            System.getProperty("bookshelf.clientID"),
            System.getProperty("bookshelf.clientSecret"),
            SCOPE)
        .build();
    String state = new BigInteger(130, new SecureRandom()).toString(32);
    setSessionVariable(sessionNum, "state", state);
    if (req.getAttribute("loginDestination") != null) {
      setSessionVariable(
          sessionNum,
          "loginDestination",
          (String) req.getAttribute("loginDestination"));
      logger.log(
          Level.INFO, "logging destination " + (String) req.getAttribute("loginDestination"));
    } else {
      setSessionVariable(sessionNum, "loginDestination", "/books");
      logger.log(Level.INFO, "logging destination /books");
    }
    // callback url should be the one registered in Google Developers Console
    String url =
        flow.newAuthorizationUrl()
        .setRedirectUri(System.getProperty("bookshelf.callback"))
        .setState(state)
        .build();
    resp.sendRedirect(url);
  }

}
// [END example]