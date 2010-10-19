/**
 * Licensed to Josh Lindsey at Cloudspace under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information 
 * regarding copyright ownership. Josh Lindsey licenses 
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package multiplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkDecoBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.EventSinkDecorator;
import com.cloudera.util.Pair;
import com.google.common.base.Preconditions;

public class JsonMultiplexDecorator<S extends EventSink> extends EventSinkDecorator<S> {
	static Logger LOG = Logger.getLogger(JsonMultiplexDecorator.class);
  private final String serverName;
  private final String logType;

  public JsonMultiplexDecorator(S s, String serverName, String logType) {
    super(s);

    this.serverName = serverName;
    this.logType = logType;

		LOG.setLevel(Level.DEBUG);
  }

  @Override
  public void append(Event e) throws IOException {
		LOG.debug("incoming event body: " + new String(e.getBody()));
		
		String body = new String(e.getBody()).replaceAll("\"", "\\\"");
		LOG.debug("new body string: " + body);
	
    String json = "{ \"server\": \"" + this.serverName + "\"," +
      "\"log_type\": \"" + this.logType + "\", " +
      "\"body\": \"" + body + "\" }";
		LOG.debug("new json string: " + json);
		LOG.debug("new json bytes: " + json.getBytes());

    EventImpl e2 = new EventImpl(json.getBytes(),
        e.getTimestamp(), e.getPriority(), e.getNanos(), e.getHost(),
        e.getAttrs());
		LOG.debug("new event: " + e2);

    super.append(e2);
  }

  public static SinkDecoBuilder builder() {
    return new SinkDecoBuilder() {
      @Override
      public EventSinkDecorator<EventSink> build(Context context,
          String... argv) {
        Preconditions.checkArgument(argv.length == 2,
            "usage: multiplexDecorator(serverName, logType)");

        return new JsonMultiplexDecorator<EventSink>(null, argv[0], argv[1]);
      }
    };
  }

  public static List<Pair<String, SinkDecoBuilder>> getDecoratorBuilders() {
    List<Pair<String, SinkDecoBuilder>> builders = 
      new ArrayList<Pair<String, SinkDecoBuilder>>();

    builders.add(new Pair<String, SinkDecoBuilder>("jsonMultiplexDecorator", builder()));

    return builders;
  }
}
