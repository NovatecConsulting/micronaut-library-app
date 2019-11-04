package utils

import io.micronaut.discovery.ServiceInstance
import io.micronaut.http.client.LoadBalancer
import org.reactivestreams.Publisher

class TestLoadBalancer: LoadBalancer {
    override fun select(discriminator: Any?): Publisher<ServiceInstance> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
