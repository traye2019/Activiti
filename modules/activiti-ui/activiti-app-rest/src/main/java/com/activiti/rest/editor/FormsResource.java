/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activiti.rest.editor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.Model;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.editor.form.FormRepresentation;
import com.activiti.repository.editor.ModelRepository;
import com.activiti.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/rest/form-models")
public class FormsResource {

  private static final Logger logger = LoggerFactory.getLogger(FormsResource.class);

  private static final int MIN_FILTER_LENGTH = 2;
  
  @Autowired
  protected ModelRepository modelRepository;

  @Autowired
  protected ObjectMapper objectMapper;

  @RequestMapping(method = RequestMethod.GET, produces = "application/json")
  public ResultListDataRepresentation getForms(@RequestParam(required = true) Long referenceId, HttpServletRequest request) {

    // need to parse the filterText parameter ourselves, due to encoding issues with the default parsing.
    String filter = null;
    List<NameValuePair> params = URLEncodedUtils.parse(request.getQueryString(), Charset.forName("UTF-8"));
    if (params != null) {
      for (NameValuePair nameValuePair : params) {
        if ("filter".equalsIgnoreCase(nameValuePair.getName())) {
          filter = nameValuePair.getValue();
        }
      }
    }
    String validFilter = makeValidFilterText(filter);

    List<Model> models = null;
    if (validFilter != null) {
      models = modelRepository.findModelsByModelTypeAndReferenceId(AbstractModel.MODEL_TYPE_FORM, validFilter, referenceId);
      List<Model> createdByModels = modelRepository.findModelsCreatedBy(SecurityUtils.getCurrentUserId(), AbstractModel.MODEL_TYPE_FORM, validFilter, new Sort(Direction.ASC, "name"));
      if (CollectionUtils.isNotEmpty(createdByModels)) {
        models.addAll(createdByModels);
      }

    } else {
      models = modelRepository.findModelsByModelTypeAndReferenceId(AbstractModel.MODEL_TYPE_FORM, referenceId);
      List<Model> createdByModels = modelRepository.findModelsCreatedBy(SecurityUtils.getCurrentUserId(), AbstractModel.MODEL_TYPE_FORM, new Sort(Direction.ASC, "name"));
      if (CollectionUtils.isNotEmpty(createdByModels)) {
        models.addAll(createdByModels);
      }
    }

    List<FormRepresentation> reps = new ArrayList<FormRepresentation>();

    for (Model model : models) {
      reps.add(new FormRepresentation(model));
    }

    Collections.sort(reps, new NameComparator());

    ResultListDataRepresentation result = new ResultListDataRepresentation(reps);
    result.setTotal(Long.valueOf(models.size()));
    return result;
  }

  protected String makeValidFilterText(String filterText) {
    String validFilter = null;

    if (filterText != null) {
      String trimmed = StringUtils.trim(filterText);
      if (trimmed.length() >= MIN_FILTER_LENGTH) {
        validFilter = "%" + trimmed.toLowerCase() + "%";
      }
    }
    return validFilter;
  }

  class NameComparator implements Comparator<FormRepresentation> {
    @Override
    public int compare(FormRepresentation o1, FormRepresentation o2) {
      return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
    }
  }
}
