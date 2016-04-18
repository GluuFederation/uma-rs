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

### Usage

```java
 // initialize protector (typically as application scope)
 final RsProtector protector = RsProtector.instance(fileInputStream("simple.json"));

 // somewhere in http interceptor/filter code
 if (!protector.hasAcess(httpMethod, presentScopes)) {
     throw new WebApplicationException(UNAUTHORIZED);
 }
```

