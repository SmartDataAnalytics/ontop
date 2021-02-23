package it.unibz.inf.ontop.protege.connection;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.JDBC_DRIVER;
import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.JDBC_URL;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.JDBC_PASSWORD;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.JDBC_USER;

public class DataSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataSource.class);
	
	private final Properties properties = new Properties();

	private final URI id;
	private String driver = "", url = "", username = "", password = "";

	private final List<DataSourceListener> listeners = new ArrayList<>();

	public DataSource() {
		this.id = URI.create(UUID.randomUUID().toString());
	}

	@Nonnull
	public String getDriver() {
		return driver;
	}

	@Nonnull
	public String getUsername() {
		return username;
	}

	@Nonnull
	public String getPassword() {
		return password;
	}

	@Nonnull
	public String getURL() {
		return url;
	}

	public void set(String url, String username, String password, String driver) {
		Objects.requireNonNull(url);
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);
		Objects.requireNonNull(driver);

		boolean changed = !this.url.equals(url) || !this.username.equals(username)
					|| !this.password.equals(password) || !this.driver.equals(driver);

		this.url = url;
		this.username = username;
		this.password = password;
		this.driver = driver;

		if (changed)
			listeners.forEach(DataSourceListener::dataSourceChanged);
	}

	public Connection getConnection() throws SQLException {
		JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
		return man.getConnection(url, username, password);
	}

	public Properties asProperties() {
		Properties p = new Properties();
		p.putAll(properties);
		p.put(OntopSQLCoreSettings.JDBC_NAME, id.toString());
		p.put(OntopSQLCoreSettings.JDBC_URL, url);
		p.put(OntopSQLCredentialSettings.JDBC_USER, username);
		p.put(OntopSQLCredentialSettings.JDBC_PASSWORD, password);
		p.put(OntopSQLCoreSettings.JDBC_DRIVER, driver);
		return p;
	}

	public void addListener(DataSourceListener listener) {
		if (listener != null && !listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeListener(DataSourceListener listener) {
		listeners.remove(listener);
	}

	public void load(File propertyFile) throws IOException {
		if (propertyFile.exists())
			try (FileReader reader = new FileReader(propertyFile)) {
				properties.load(reader);
				updateDataSourceParametersFromUserSettings();
			}
	}

	public void update(Properties p) {
		properties.putAll(p);
		updateDataSourceParametersFromUserSettings();
	}

	public void reset() {
		properties.clear();
		set("", "", "", "");
	}

	public void store(File propertyFile) throws IOException {
		Properties properties = asProperties();

		// Generate a property file iff there is at least one property that is not "jdbc.name"
		if (properties.entrySet().stream()
				.anyMatch(e -> !e.getKey().equals(OntopSQLCoreSettings.JDBC_NAME) &&
						!e.getValue().equals(""))){
			try (FileOutputStream outputStream = new FileOutputStream(propertyFile)) {
				properties.store(outputStream, null);
			}
			LOGGER.info("Property file saved to {}", propertyFile.toPath());
		}
		else {
			Files.deleteIfExists(propertyFile.toPath());
		}
	}

	private void updateDataSourceParametersFromUserSettings() {
		set(Optional.ofNullable(properties.getProperty(JDBC_URL))
						.orElseGet(this::getURL),
				Optional.ofNullable(properties.getProperty(JDBC_USER))
						.orElseGet(this::getUsername),
				Optional.ofNullable(properties.getProperty(JDBC_PASSWORD))
						.orElseGet(this::getPassword),
				Optional.ofNullable(properties.getProperty(JDBC_DRIVER))
						.orElseGet(this::getDriver));
	}
}
