/**
 * Copyright 2012 Impetus Infotech.
 *
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
 */
package com.impetus.kundera.graph;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.impetus.kundera.configure.Configurator;
import com.impetus.kundera.persistence.context.PersistenceCache;

/**
 * Test case for {@link ObjectGraphBuilder}
 * 
 * @author amresh.singh
 */
public class ObjectGraphBuilderTest
{
    ObjectGraphBuilder graphBuilder;

    Configurator configurator = new Configurator("kunderatest");

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        configurator.configure();
        graphBuilder = new ObjectGraphBuilder();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {

    }

    /**
     * Test method for
     * {@link com.impetus.kundera.graph.ObjectGraphBuilder#getObjectGraph(java.lang.Object)}
     * .
     */
    @Test
    public void testGetObjectGraph()
    {
        Store store = new Store(1, "Food Bazaar, Noida");
        store.addCounter(new BillingCounter(1, "A"));
        store.addCounter(new BillingCounter(2, "B"));
        store.addCounter(new BillingCounter(3, "C"));

        ObjectGraph graph = graphBuilder.getObjectGraph(store, null, new PersistenceCache());

        Assert.assertNotNull(graph);
        Node headNode = graph.getHeadNode();
        Map<String, Node> nodeMappings = graph.getNodeMapping();

        Assert.assertNotNull(headNode);
        Assert.assertNotNull(nodeMappings);
        Assert.assertFalse(nodeMappings.isEmpty());
        Assert.assertEquals(4, nodeMappings.size());

        Assert.assertTrue(headNode.getParents() == null);
        Assert.assertEquals(3, headNode.getChildren().size());
    }

    /**
     * Test method for
     * {@link com.impetus.kundera.graph.ObjectGraphBuilder#getNodeId(java.lang.Object, java.lang.Object)}
     * .
     */
    @Test
    public void testGetNodeId()
    {
    }

}