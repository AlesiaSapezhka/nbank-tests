package requests.skelethon.interfaces;

import models.BaseModel;


public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object post();
    Object get(Integer id);
    Object get();
    Object update(BaseModel model);
    Object delete(int id);
}
