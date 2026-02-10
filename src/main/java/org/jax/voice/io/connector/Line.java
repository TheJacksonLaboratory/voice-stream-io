package org.jax.voice.io.connector;

import java.util.Map;

import org.jax.voice.domain.Located;

record Line(Located loc, Map<String, Object> meta) {

}
