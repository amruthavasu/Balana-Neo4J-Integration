package org.wso2.balana.neo4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecuteQuery implements DBProperties {
	public static Connection createConnection() {
		Connection con = null;
		try {
			con = DriverManager.getConnection(DBProperties.CONNECTION_STRING, DBProperties.USERNAME,
					DBProperties.PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}

	public static List<String> execute(Map<String, String> attributes) {
		List<String> policyIds = new ArrayList<String>();
		String query = "";
		for (String str : attributes.keySet()) {
			if (!str.contains("-") && !str.equals("action")) {
				if (!query.equals("")) {
					query += " UNION ";
				}
				if (str.equals("environment")) {
					query += "MATCH (policy)-[:DEFINED_BY]->(" + str + ") WHERE " + str
							+ ".AttributeValue=\'" + attributes.get(str) + "\' RETURN DISTINCT policy.filename";
				} else {
					query += "MATCH (policy)-[:ASSOCIATED_WITH]->(target)-[:DEFINED_BY]->(" + str + ") WHERE " + str
						+ ".AttributeValue=\'" + attributes.get(str) + "\' RETURN DISTINCT policy.filename";
				}
			}
		}
		try {
			ResultSet resultSet = createConnection().createStatement().executeQuery(query);
			while (resultSet.next()) {
				policyIds.add(resultSet.getString("policy.filename"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return policyIds;
	}
}
