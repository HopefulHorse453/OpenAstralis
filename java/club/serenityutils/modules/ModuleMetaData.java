package club.serenityutils.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// this is used so we can get info about the module from the backend.
@Getter
@Setter
@AllArgsConstructor
public class ModuleMetaData {
    public String name, description;
}