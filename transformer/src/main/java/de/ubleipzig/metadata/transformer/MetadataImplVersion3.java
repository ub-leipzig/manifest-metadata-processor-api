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

package de.ubleipzig.metadata.transformer;

import static de.ubleipzig.metadata.transformer.MetadataApiEnum.AUTHOR;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.COLLECTION;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.DISPLAYORDER;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.GND;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.LABEL;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.LANGUAGE;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.MANIFESTTYPE;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.MANUSCRIPT;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.PHYSICAL_DESCRIPTION;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.STRUCTTYPE;
import static de.ubleipzig.metadata.transformer.MetadataApiEnum.SUBTITLE;
import static java.util.Optional.ofNullable;

import de.ubleipzig.metadata.templates.metsmods.MetsMods;
import de.ubleipzig.metadata.templates.v3.MetadataVersion3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class MetadataImplVersion3 extends MetadataObjectTypes implements MetadataApi<MetadataVersion3> {
    private static final ResourceBundle deutschLabels = ResourceBundle.getBundle("metadataLabels", Locale.GERMAN);
    private static final ResourceBundle englishLabels = ResourceBundle.getBundle("metadataLabels", Locale.ENGLISH);
    private static final String PERIOD = ".";
    private static final String ENGLISH = "en";
    private static final String DEUTSCH = "de";
    private static final String NONE = "@none";
    private MetsMods metsMods;
    private List<MetadataVersion3> finalMetadata = new ArrayList<>();

    public MetadataImplVersion3() {
    }

    @Override
    public List<MetadataVersion3> getMetadata() {
        return finalMetadata;
    }

    @Override
    public void setMetsMods(final MetsMods metsMods) {
        this.metsMods = metsMods;
    }

    private List<MetadataVersion3> setAuthor(List<MetadataVersion3> mList, Map<String, String> authorMap) {
        final Optional<String> gnd = ofNullable(authorMap.get(GND.getApiKey()));
        final String authorKey = AUTHOR.getApiKey();
        final String authorLabel = englishLabels.getString(authorKey);
        final String displayOrderKey = authorKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer authorLabelDisplayOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        if (gnd.isPresent()) {
            final String author = authorMap.get(LABEL.getApiKey());
            final String authorValue = author + " [" + gnd.get() + "]";
            final MetadataVersion3 m1 = buildMetadata(ENGLISH, authorLabel, authorValue, authorLabelDisplayOrder);
            mList.add(m1);
        } else {
            final String authorValue = authorMap.get(LABEL.getApiKey());
            final MetadataVersion3 m1 = buildMetadata(ENGLISH, authorLabel, authorValue, authorLabelDisplayOrder);
            mList.add(m1);
        }
        return mList;
    }

    private MetadataVersion3 buildMetadata(final String language, final String label, final String value,
                                           final Integer displayOrder) {
        final MetadataVersion3 metadata = new MetadataVersion3();

        final List<String> labels = new ArrayList<>();
        labels.add(label);
        Map<String, List<String>> labelsMap = new HashMap<>();
        if (language.equals(ENGLISH)) {
            labelsMap.put(ENGLISH, labels);
        } else if (language.equals(DEUTSCH)) {
            labelsMap.put(DEUTSCH, labels);
        }
        metadata.setLabel(labelsMap);

        final List<String> values = new ArrayList<>();
        values.add(value);
        final Map<String, List<String>> valuesMap = new HashMap<>();
        valuesMap.put(NONE, values);
        metadata.setValue(valuesMap);
        metadata.setDisplayOrder(displayOrder);
        return metadata;
    }

    private List<MetadataVersion3> setAuthors() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final String authorKey = AUTHOR.getApiKey();
        List<MetadataVersion3> mList = new ArrayList<>();
        if (getValueAsMap(newMetadata, authorKey).isPresent()) {
            final Map<String, String> authorMap = getValueAsMap(newMetadata, authorKey).get();
            mList = setAuthor(mList, authorMap);
        } else if (getValueAsMapList(newMetadata, authorKey).isPresent()) {
            final List<Map<String, String>> authors = getValueAsMapList(newMetadata, authorKey).get();
            for (Map<String, String> authorMap : authors) {
                mList = setAuthor(mList, authorMap);
            }
        }
        return mList;
    }

    private List<MetadataVersion3> addMetadataStringOrList(final String language,
                                                           final Map<String, Object> newMetadata, final String key,
                                                           final String displayLabel, Integer displayOrder) {
        final List<MetadataVersion3> mList = new ArrayList<>();
        if (getValueAsString(newMetadata, key).isPresent()) {
            final String value = getValueAsString(newMetadata, key).get();
            final MetadataVersion3 m1 = buildMetadata(language, displayLabel, value, displayOrder);
            mList.add(m1);
        } else if (getValueAsStringList(newMetadata, key).isPresent()) {
            final List<String> values = getValueAsStringList(newMetadata, key).get();
            values.forEach(v -> {
                final MetadataVersion3 m1 = buildMetadata(language, displayLabel, v, displayOrder);
                mList.add(m1);
            });
        }
        return mList;
    }

    private List<MetadataVersion3> setCollections() {
        final String collectionKey = COLLECTION.getApiKey();
        final String collectionLabel = englishLabels.getString(collectionKey);
        final String displayOrderKey = collectionKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer collectionLabelOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        return addMetadataStringOrList(ENGLISH, newMetadata, collectionKey, collectionLabel, collectionLabelOrder);
    }

    private List<MetadataVersion3> setLanguages() {
        final String languageKey = LANGUAGE.getApiKey();
        final String languageLabel = englishLabels.getString(languageKey);
        final String displayOrderKey = languageKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer languageLabelOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        return addMetadataStringOrList(ENGLISH, newMetadata, languageKey, languageLabel, languageLabelOrder);
    }

    private List<MetadataVersion3> addMetadataObject(final String language, final Map<String, Object> newMetadata,
                                                     final String key, final String label, final Integer displayOrder) {
        final Optional<String> value = getValueAsString(newMetadata, key);
        if (value.isPresent()) {
            final MetadataVersion3 m = buildMetadata(language, label, value.get(), displayOrder);
            finalMetadata.add(m);
        }
        return finalMetadata;
    }

    @Override
    public void buildFinalMetadata() {
        final Map<String, Object> newMetadata = metsMods.getMetadata();
        final List<MetadataVersion3> authors = setAuthors();
        final List<MetadataVersion3> collections = setCollections();
        final List<MetadataVersion3> languages = setLanguages();
        finalMetadata.addAll(authors);
        finalMetadata.addAll(collections);
        finalMetadata.addAll(languages);

        final String manifestTypeKey = MANIFESTTYPE.getApiKey();
        final Optional<String> manifestType = getValueAsString(newMetadata, manifestTypeKey);
        if (manifestType.isPresent()) {
            final String manifestTypeLabel = englishLabels.getString(manifestTypeKey);
            final String manifestTypeLabelDisplayOrderKey = manifestTypeKey + PERIOD + DISPLAYORDER.getApiKey();
            final Integer displayOrder = Integer.valueOf(englishLabels.getString(manifestTypeLabelDisplayOrderKey));
            final MetadataVersion3 manifestTypeObj = buildMetadata(
                    ENGLISH, manifestTypeLabel, manifestType.get(), displayOrder);
            finalMetadata.add(manifestTypeObj);
            if (manifestType.get().equals(MANUSCRIPT.getApiKey())) {
                //hack for UBL mets/mods classification confusion
                finalMetadata = addMetadataObject(DEUTSCH, newMetadata, SUBTITLE.getApiKey(), "Objekttitel", 2);
            } else {
                finalMetadata = addMetadataObject(ENGLISH, newMetadata, SUBTITLE.getApiKey(), "Subtitle", 2);
                //only show Physical Dimension for Non-manuscripts
                finalMetadata = addMetadataObject(
                        ENGLISH, newMetadata, PHYSICAL_DESCRIPTION.getApiKey(), "Physical Description", 10);
            }
        }

        final Set<String> enFilteredLabels = buildFilteredLabelSet(englishLabels);
        final Set<String> deFilteredLabels = buildFilteredLabelSet(deutschLabels);
        setFilteredLabelMetadata(newMetadata, enFilteredLabels, englishLabels);
        setFilteredLabelMetadata(newMetadata, deFilteredLabels, deutschLabels);
        finalMetadata.sort(Comparator.comparing(MetadataVersion3::getDisplayOrder));
    }

    private void setFilteredLabelMetadata(final Map<String, Object> newMetadata, final Set<String> filteredLabels,
                                          final ResourceBundle bundle) {
        filteredLabels.forEach(l -> {
            final String displayLabel = bundle.getString(l);
            final String displayOrderKey = l + PERIOD + DISPLAYORDER.getApiKey();
            final Integer displayOrder = Integer.valueOf(bundle.getString(displayOrderKey));
            final String languageTag = bundle.getLocale().toLanguageTag();
            if (languageTag.equals(ENGLISH)) {
                finalMetadata = addMetadataObject(ENGLISH, newMetadata, l, displayLabel, displayOrder);
            } else if (languageTag.equals(DEUTSCH)) {
                finalMetadata = addMetadataObject(DEUTSCH, newMetadata, l, displayLabel, displayOrder);
            }
        });
    }

    @Override
    public List<MetadataVersion3> buildStructureMetadataForId(final String structId) {
        final List<MetadataVersion3> mList = new ArrayList<>();
        final String authorKey = AUTHOR.getApiKey();
        final String authorNameKey = LABEL.getApiKey();
        final String authorLabel = englishLabels.getString(authorKey);
        final String displayOrderKey = authorKey + PERIOD + DISPLAYORDER.getApiKey();
        final Integer authorLabelDisplayOrder = Integer.valueOf(englishLabels.getString(displayOrderKey));
        final List<Map<String, Object>> structureMetadata = metsMods.getStructures();
        final Optional<List<Map<String, Object>>> filteredSubList = Optional.of(
                structureMetadata.stream().filter(s -> s.containsValue(structId)).collect(Collectors.toList()));
        filteredSubList.ifPresent(maps -> maps.forEach(sm -> {
            if (getValueAsMap(sm, authorKey).isPresent()) {
                final Map<String, String> authorMap = getValueAsMap(sm, authorKey).get();
                final Optional<String> gnd = ofNullable(authorMap.get(GND.getApiKey()));
                if (gnd.isPresent()) {
                    final String author = authorMap.get(authorNameKey);
                    final String authorValue = author + " [" + gnd.get() + "]";
                    final MetadataVersion3 m1 = buildMetadata(
                            ENGLISH, authorLabel, authorValue, authorLabelDisplayOrder);
                    mList.add(m1);
                } else {
                    final String authorValue = authorMap.get(authorNameKey);
                    final MetadataVersion3 m1 = buildMetadata(
                            ENGLISH, authorLabel, authorValue, authorLabelDisplayOrder);
                    mList.add(m1);
                }
            }
            final String structureTypeKey = STRUCTTYPE.getApiKey();
            final Optional<String> structureType = getValueAsString(sm, structureTypeKey);
            if (structureType.isPresent()) {
                final String structureTypeLabel = englishLabels.getString(structureTypeKey);
                final MetadataVersion3 structureTypeObj = buildMetadata(
                        ENGLISH, structureTypeLabel, structureType.get(), 1);
                mList.add(structureTypeObj);
            }
        }));
        if (!mList.isEmpty()) {
            return mList;
        } else {
            return null;
        }
    }
}
