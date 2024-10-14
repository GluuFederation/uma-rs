# Moved to [Gluu4 monorepo](https://github.com/GluuFederation/gluu4/tree/main/uma-rs)
# uma-rs
UMA Resource Server library - helps to easily protect Java based project with UMA in declarative way.


### Sample declaration

```json
{"resources":[
    {
        "path":"/photo",
        "conditions":[
            {
                "httpMethods":["GET"],
                "scopes":[
                    "http://photoz.example.com/dev/actions/view"
                ]
            },
            {
                "httpMethods":["PUT", "POST"],
                "scopes":[
                    "http://photoz.example.com/dev/actions/all",
                    "http://photoz.example.com/dev/actions/add"
                ],
                "ticketScopes":[
                    "http://photoz.example.com/dev/actions/add"
                ]
            }
        ]
    },
    {
        "path":"/document",
        "conditions":[
            {
                "httpMethods":["GET"],
                "scopes":[
                    "http://photoz.example.com/dev/actions/view"
                ]
            }
        ]
    }
]
}
```

`ticketScopes` are used for UMA ticket registration. If it is skipped then ALL scopes are registered for ticket.

### Usage

```java
Configuration configuration = ConfigurationLoader.loadFromJson(inputStream(CONFIGURATION_FILE_NAME));
Collection<RsResource> values = RsProtector.instance(inputStream(PROTECTION_CONFIGURATION_FILE_NAME)).getResourceMap().values();

ServiceProvider serviceProvider = new ServiceProvider(configuration);
PatProvider patProvider = new PatProvider(serviceProvider);
ResourceRegistrar resourceRegistrar = new ResourceRegistrar(patProvider);

resourceRegistrar.register(values);
```

Check [RS demo](https://github.com/GluuFederation/oxUmaDemo/tree/master/RS) project sources.

