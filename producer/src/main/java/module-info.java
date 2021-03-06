/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module de.ubleipzig.metadata.producer {
    exports de.ubleipzig.metadata.producer;
    exports de.ubleipzig.metadata.producer.doc;
    requires de.ubleipzig.metadata.processor;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires de.ubleipzig.iiif.vocabulary;
    requires de.ubleipzig.metadata.extractor;
    requires de.ubleipzig.metadata.templates;
    requires org.apache.commons.rdf.api;
    requires slf4j.api;
    requires org.apache.commons.rdf.simple;
    requires com.fasterxml.jackson.annotation;
    requires java.validation;
    requires xmlprojector;
    requires camel.core;
    requires de.ubleipzig.metadata.transformer;

}