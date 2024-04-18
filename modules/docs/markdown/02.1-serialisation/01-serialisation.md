---
sidebar_label: Serialisation overview
title: Serialisation overview
---

The code generated by Smithy4s is strictly **protocol agnostic**. One implication is that the data-types generated by Smithy4s are not tied to any particular serialisation format or third-party library. Instead, Smithy4s generates an instance of a `smithy4s.schema.Schema` for each data-type (see [the relevant section](../05-design/02-schemas.md)). From this schema can be derived encoders and decoders for virtually any serialisation format.

Smithy4s provides opt-in modules implementing serialisation in a bunch of formats, including `JSON`, `XML` and `Protobuf`. The modules cross-compile to all combinations of platforms (JVM/JS/Native) and scala-versions supported by Smithy4s.

### Document (JSON-like adt)

The `smithy4s-core` module provides a `smithy4s.Document` datatype that is used in code-generation when [document](https://smithy.io/2.0/spec/simple-types.html#document) shapes are used in smithy. `Document` is effectively a JSON ADT, and can be easily converted to from other ADTs, such as the one provided by the [Circe library](https://circe.github.io/circe/).

`Document` also comes with its own Encoder and Decoder construct, for which instances can be derived for every datatype generated by Smithy4s.

```scala mdoc:reset
import smithy4s.example.hello.Person
import smithy4s.Document

val personEncoder = Document.Encoder.fromSchema(Person.schema)
val personDocument = personEncoder.encode(Person(name = "John Doe"))

val personDecoder = Document.Decoder.fromSchema(Person.schema)
val maybePerson = personDecoder.decode(personDocument)
```

By default, smithy4s Documents abide by the same semantics as `smithy4s-json` (see section below).

It is worth noting that, although `Document` is isomorphic to a JSON ADT, its `.toString` is not valid JSON. Likewise, the `smithy4s-core` module does not contain logic to parse JSON strings into Documents. In order to read/write Documents from/to JSON strings, you need the `smithy4s-json` module. The `smithy4s.json.Json` entry-point contains methods that work with Documents.

### JSON

The `smithy4s-json` module provides [jsoniter-based](https://github.com/plokhotnyuk/jsoniter-scala) encoders/decoders that can read/write generated data-types from/to JSON bytes/strings, without an intermediate JSON ADT. The performance of this module is very competitive are [very competitive](https://plokhotnyuk.github.io/jsoniter-scala/) in the Scala ecosystem.

This module is provided at the following coordinates :

```
sbt : "com.disneystreaming.smithy4s" %% "smithy4s-json" % "@VERSION@"
mill : "com.disneystreaming.smithy4s::smithy4s-json:@VERSION@"
```

The entrypoint for JSON parsing/writing is `smithy4s.json.Json`. See below for example usage.

```scala mdoc:reset
import smithy4s.example.hello.Person

import smithy4s.Blob
import smithy4s.json.Json

val personEncoder = Json.payloadCodecs.encoders.fromSchema(Person.schema)
val personJSON = personEncoder.encode(Person(name = "John Doe")).toUTF8String

val personDecoder = Json.payloadCodecs.decoders.fromSchema(Person.schema)
val maybePerson = personDecoder.decode(Blob(personJSON))
```

By default, `smithy4s-json` abides by the semantics of :

* [official smithy traits](https://smithy.io/2.0/spec/protocol-traits.html), including:
  * [jsonName](https://smithy.io/2.0/spec/protocol-traits.html#jsonname-trait)
  * [timestampFormat](https://smithy.io/2.0/spec/protocol-traits.html#timestampformat-trait)
  * [sparse](https://smithy.io/2.0/spec/type-refinement-traits.html#sparse-trait)
  * [required](https://smithy.io/2.0/spec/type-refinement-traits.html#required-trait)
  * [default](https://smithy.io/2.0/spec/type-refinement-traits.html#default-value-serialization). It is worth noting that, by default, Smithy4s chooses to not serialise default values if the when the member is optional.
* [alloy traits](https://github.com/disneystreaming/alloy/blob/main/docs/serialisation/json.md)


### XML

The `smithy4s-xml` module provides [fs2-data](https://fs2-data.gnieh.org/documentation/xml/) encoders/decoders that can read/write generated data-types from/to XML bytes/strings. It is provided at the following coordinates :

```
sbt : "com.disneystreaming.smithy4s" %% "smithy4s-xml" % "@VERSION@"
mill : "com.disneystreaming.smithy4s::smithy4s-xml:@VERSION@"
```

The entrypoint for  `smithy4s.xml.Xml`. See below for example usage.

```scala mdoc:reset
import smithy4s.example.hello.Person

import smithy4s.Blob
import smithy4s.xml.Xml

val personEncoder = Xml.encoders.fromSchema(Person.schema)
val personXML = personEncoder.encode(Person(name = "John Doe")).toUTF8String

val personDecoder = Xml.decoders.fromSchema(Person.schema)
val maybePerson = personDecoder.decode(Blob(personXML))
```

By default, `smithy4s-xml` abides by the semantics of :

* [official XML-related smithy traits](https://smithy.io/2.0/spec/protocol-traits.html#xml-bindings)

### Protobuf

The `smithy4s-protobuf` module provides [protocol-buffers](https://protobuf.dev/) codecs that can read/write generated data-types from protobuf-encoded bytes.

```
sbt : "com.disneystreaming.smithy4s" %% "smithy4s-protobuf" % "@VERSION@"
mill : "com.disneystreaming.smithy4s::smithy4s-protobuf:@VERSION@"
```

The entrypoint for Protobuf parsing/writing is `smithy4s.protobuf.Protobuf`. See below for example usage.

```scala mdoc:reset
import smithy4s.example.hello.Person
import smithy4s.protobuf.Protobuf

val personCodec = Protobuf.codecs.fromSchema(Person.schema)
val personBytes = personCodec.writeBlob(Person(name = "John Doe"))
val maybePerson = personCodec.readBlob(personBytes)
```

By default, `smithy4s-protobuf` abides by the semantics of :

* [alloy protobuf traits](https://github.com/disneystreaming/alloy/blob/main/docs/serialisation/protobuf.md). These semantics are the exact same semantics that [smithy-translate](https://github.com/disneystreaming/smithy-translate) uses to translate smithy to protobuf. This implies that the Smithy4s protobuf codecs are compatible with the codecs of other protobuf tools, generated from the .proto files resulting from running smithy through smithy-translate. In short, Smithy4s and [ScalaPB](https://github.com/scalapb/ScalaPB) can talk to each other : the ScalaPB codecs generated from protobuf after a translation from smithy are able to decode binary data produced by Smithy4s protobuf codecs (and vice versa).


```
┌────────────────────┐                        ┌────────────────────┐
│                    │                        │                    │
│                    │                        │                    │
│                    │                        │                    │
│                    │                        │                    │
│     Smithy IDL     ├────────────────────────►    Protobuf IDL    │
│                    │   smithy-translate     │                    │
│                    │                        │                    │
│                    │                        │                    │
│                    │                        │                    │
└─────────┬──────────┘                        └─────────┬──────────┘
          │                                             │
          │                                             │
          │                                             │
          │                                             │
          │                                             │
          │                                             │
          │ Smithy4s codegen                            │ ScalaPB codegen
          │                                             │
          │                                             │
          │                                             │
          │                                             │
          │                                             │
┌─────────▼──────────┐                        ┌─────────▼──────────┐
│                    │                        │                    │
│                    │                        │                    │
│                    │                        │                    │
│                    ◄────────────────────────┤                    │
│    Smithy4s code   │  Runtime communication │     ScalaPB code   │
│                    ├────────────────────────►                    │
│                    │                        │                    │
│                    │                        │                    │
│                    │                        │                    │
└────────────────────┘                        └────────────────────┘
```