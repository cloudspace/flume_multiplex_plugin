/**
 * Licensed to Josh Lindsey at Cloudspace under one
 * or more contributor license agreements. Josh Lindsey licenses 
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

import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.util.Pair;
import com.google.common.base.Preconditions;

public class TcpSink extends EventSink.Base {
  static Logger LOG = Logger.getLogger(TcpSink.class);
  private Socket socket;
  private final String serverAddress;
  private final int serverPort;

  public TcpSink(String serverAddress, int serverPort) {
    super();

    this.serverAddress = serverAddress;
    this.serverPort = serverPort;
  }

  @Override
  public void open() throws IOException {
    this.socket = new Socket(this.serverAddress, this.serverPort);
  }

  @Override
  public void append(Event e) throws IOException {
    String body = new String(e.getBody());
    this.socket.getOutputStream().write(body.getBytes());
  }

  @Override
  public void close() throws IOException {
    this.socket.close();
  }

  public static SinkBuilder builder() {
    return new SinkBuilder() {
      @Override
      public EventSink build(Context context, String... argv) {
        Preconditions.checkArgument(argv.length == 2,
            "usage: tcpSink(serverAddress, serverPort)");

        return new TcpSink(argv[0], Integer.valueOf(argv[1]));
      }
    };
  }

  public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
    List<Pair<String, SinkBuilder>> builders =
      new ArrayList<Pair<String, SinkBuilder>>();
    builders.add(new Pair<String, SinkBuilder>("tcpSink", builder()));

    return builders;
  }
}
