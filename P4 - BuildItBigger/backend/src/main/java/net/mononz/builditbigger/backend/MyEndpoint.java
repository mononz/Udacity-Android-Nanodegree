/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package net.mononz.builditbigger.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import net.mononz.joker.lib.JokeSource;

@Api(
  name = "myApi",
  version = "v1",
  namespace = @ApiNamespace(
    ownerDomain = "backend.builditbigger.mononz.net",
    ownerName = "backend.builditbigger.mononz.net",
    packagePath=""
  )
)
public class MyEndpoint {

    @ApiMethod(name = "tastyJoker")
    public MyBean tastyJoker() {
        MyBean response = new MyBean();
        response.setData(JokeSource.getJoke());
        return response;
    }

}
