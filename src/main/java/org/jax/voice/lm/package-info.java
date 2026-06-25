/*-
 * Copyright (c) 2026 Jackson Laboratory
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License  Version 2.0, January 2004
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 */
/**
 * Shared wire-format and schema types for the LMDB-backed voice query store.
 *
 * The {@code voice-bulk-import} module writes data using these definitions;
 * the {@code voice-graph-service} module reads using the same definitions.
 * Anything that must be byte-identical between writer and reader belongs here.
 *
 * Contents:
 * <ul>
 *   <li>{@link org.jax.voice.lm.Codec} — sorted-int delta+varint encoder and decoder.</li>
 *   <li>{@link org.jax.voice.lm.LMSchema} — DBI names, peak_meta layout constants,
 *       chromosome byte encoding helpers, and bitmap filename sanitiser.</li>
 *   <li>{@link org.jax.voice.lm.PeakMeta} — packed 10-byte peak_meta record.</li>
 * </ul>
 *
 * Note: the in-memory ingest helpers (Object2IntOpenHashMap accumulators,
 * external sort buffer, etc.) live in voice-bulk-import — they are not part
 * of the wire contract.
 */
package org.jax.voice.lm;
