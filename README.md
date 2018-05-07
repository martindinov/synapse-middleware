# synapse-middleware
A middleware software for easier, more efficient and more powerful BCI development

We have not worked on this project for a while. We may get back to it at some point in the near future, or not. In the mean time, if anyone wants to work on middleware, this may be a starting point.

Synapse is based around the use of MQTT, which is a neat and lightweight publish-subscribe protocol. Currently, a lot of BCI libraries are product-specific. We started writing this partly out of personal frustration, as there is a lot of duplication of effort when we have to write the same boilerplate code for different headsets/hardware. With Synapse, we wanted a uniform interface for connecting different BCI devices to a central server, with the flexibility of specifying device-specific options.