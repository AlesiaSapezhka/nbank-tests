package requests.skeleton.interfaces;

import models.BaseModel;


public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(Integer id);
    Object update(int id, BaseModel model);
    Object delete(int id);
}
