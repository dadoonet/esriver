/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.river.es;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * @author dadoonet (David Pilato)
 */
public class EsRiverTest {

	public static void main(String[] args) throws Exception {
//		String url = "http://www.lemonde.fr/rss/une.xml";

		Node node = NodeBuilder
				.nodeBuilder()
				.settings(
						ImmutableSettings.settingsBuilder()
						.put("gateway.type", "local")
						.put("path.data", "./target/es/data")		
						.put("path.logs", "./target/es/logs")		
						.put("path.work", "./target/es/work")		
						).node();
		
		// We wait for one second to let ES start
		Thread.sleep(1000);
		try {
			node.client().admin().indices()
					.delete(new DeleteIndexRequest("_river")).actionGet();
			// We wait for one second to let ES delete the river
			Thread.sleep(1000);
		} catch (IndexMissingException e) {
			// Index does not exist... Fine
		}

		try {
			node.client().admin().indices().delete(new DeleteIndexRequest("essource")).actionGet();
			// We wait for one second to let ES delete indexes
		} catch (IndexMissingException e) {
			// Indexes does not exist... Fine
		}
		try {
			node.client().admin().indices().delete(new DeleteIndexRequest("escopy")).actionGet();
			// We wait for one second to let ES delete indexes
		} catch (IndexMissingException e) {
			// Indexes does not exist... Fine
		}
		Thread.sleep(1000);

		// Creating the Elastic Search source
		node.client().admin().indices().create(new CreateIndexRequest("essource")).actionGet();
		XContentBuilder xbOldMapping = jsonBuilder()
				.startObject()
					.startObject("tweet")
						.startObject("properties")
							.startObject("author")
								.field("type", "string")
							.endObject()
							.startObject("description")
								.field("type", "string")
							.endObject()
							.startObject("date")
								.field("type", "date")
							.endObject()
						.endObject()
					.endObject()
				.endObject();
		node.client().admin().indices()
			.preparePutMapping("essource")
			.setType("tweet")
			.setSource(xbOldMapping)
			.execute().actionGet();

		// Creating the Elastic Search destination (with a new mapping)
		node.client().admin().indices().create(new CreateIndexRequest("escopy")).actionGet();
		XContentBuilder xbNewMapping = jsonBuilder()
				.startObject()
					.startObject("tweet")
						.startObject("properties")
							.startObject("author")
								.field("type", "string")
							.endObject()
							.startObject("description")
								.field("type", "string")
								.field("analyzer", "french")
							.endObject()
							.startObject("date")
								.field("type", "date")
							.endObject()
						.endObject()
					.endObject()
				.endObject();
		node.client().admin().indices()
			.preparePutMapping("escopy")
			.setType("tweet")
			.setSource(xbNewMapping)
			.execute().actionGet();
		

//		// We create a rss feed on the same cluster
//		int updateRate = 10 * 1000;
//		XContentBuilder xb = jsonBuilder()
//				.startObject()
//					.field("type", "es")
//					.startObject("es")
//						.field("url", url)
//						.field("update_rate", updateRate)
//					.endObject()
//				.endObject();
//
//		node.client().prepareIndex("_river", "es", "_meta").setSource(xb)
//				.execute().actionGet();

		// Let's wait one hour 
		Thread.sleep(1 * 60 * 60 * 1000);
	}
}
