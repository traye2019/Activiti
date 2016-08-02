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
package com.activiti.repository.editor;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.editor.Model;

/**
 * Spring Data JPA repository for the Model entity.
 */
public interface ModelRepository extends JpaRepository<Model, Long> {

  @Query("from Model as model where model.createdBy = :user and (model.modelType is null or model.modelType = 0 or model.modelType = 1) and model.referenceId is null")
  List<Model> findProcessesCreatedBy(@Param("user") String createdBy, Sort sort);

  @Query("from Model as model where model.createdBy = :user and "
      + "(lower(model.name) like :filter or lower(model.description) like :filter) and (model.modelType is null or model.modelType = 0 or model.modelType = 1) and model.referenceId is null")
  List<Model> findProcessesCreatedBy(@Param("user") String createdBy, @Param("filter") String filter, Sort sort);

  @Query("from Model as model where model.createdBy = :user and model.modelType = :modelType and model.referenceId is null")
  List<Model> findModelsCreatedBy(@Param("user") String createdBy, @Param("modelType") Integer modelType, Sort sort);

  @Query("from Model as model where model.createdBy = :user and "
      + "(lower(model.name) like :filter or lower(model.description) like :filter) and model.modelType = :modelType and model.referenceId is null")
  List<Model> findModelsCreatedBy(@Param("user") String createdBy, @Param("modelType") Integer modelType, @Param("filter") String filter, Sort sort);

  @Query("from Model as model where model.referenceId = :referenceId")
  List<Model> findModelsByReferenceId(@Param("referenceId") Long referenceId);

  @Query("from Model as model where model.modelType = :modelType and model.referenceId = :referenceId")
  List<Model> findModelsByModelTypeAndReferenceId(@Param("modelType") Integer modelType, @Param("referenceId") Long referenceId);

  @Query("from Model as model where (lower(model.name) like :filter or lower(model.description) like :filter) " + "and model.modelType = :modelType and model.referenceId = :referenceId")
  List<Model> findModelsByModelTypeAndReferenceId(@Param("modelType") Integer modelType, @Param("filter") String filter, @Param("referenceId") Long referenceId);

  @Query("from Model as model where model.modelType = :modelType and (model.referenceId = :referenceId or model.referenceId is null)")
  List<Model> findModelsByModelTypeAndReferenceIdOrNullReferenceId(@Param("modelType") Integer modelType, @Param("referenceId") Long referenceId);

  @Query("select count(m.id) from Model m where m.createdBy = :user and m.modelType = :modelType")
  Long countByModelTypeAndUser(@Param("modelType") int modelType, @Param("user") String user);
  
  @Query("select m from ModelRelation mr inner join mr.model m where mr.parentModelId = :parentModelId")
  List<Model> findModelsByParentModelId(@Param("parentModelId") Long parentModelId);
  
  @Query("select m from ModelRelation mr inner join mr.model m where mr.parentModelId = :parentModelId and m.modelType = :modelType")
  List<Model> findModelsByParentModelIdAndType(@Param("parentModelId") Long parentModelId, @Param("modelType") Integer modelType);
  
  @Query("select m.id, m.name, m.modelType from ModelRelation mr inner join mr.parentModel m where mr.modelId = :modelId")
  List<Model> findModelsByChildModelId(@Param("modelId") Long modelId);
}
