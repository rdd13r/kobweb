package com.varabyte.kobweb.project.conf

import com.charleskorn.kaml.Yaml
import com.varabyte.kobweb.common.yaml.nonStrictDefault
import com.varabyte.kobweb.project.KobwebFolder
import com.varabyte.kobweb.project.io.KobwebReadableTextFile

class KobwebConfFile(kobwebFolder: KobwebFolder) : KobwebReadableTextFile<KobwebConf>(
    kobwebFolder,
    "conf.yaml",
    deserialize = { text -> Yaml.nonStrictDefault.decodeFromString(KobwebConf.serializer(), text) }
)
