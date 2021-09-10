package krsdm.springbootcrud.contollers;

import krsdm.springbootcrud.models.User;
import krsdm.springbootcrud.service.RoleService;
import krsdm.springbootcrud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    AdminController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping()
    public String userList(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("{id}")
    public String showUser(@PathVariable("id") long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/user";
    }

    @GetMapping("new")
    public String newUser(Model model) {
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("user", new User());
        return "admin/new";
    }

    @PostMapping("new")
    public String createUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {

        // Если есть ошибки, попросим исправить
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getRoles());
            return "admin/new";
        }

        // Если юзер с таким именем уже существует, сообщим об этом
        if (userService.getUserByName(user.getName()) != null) {
            bindingResult.addError(new FieldError("name", "name",
                    String.format("User with name \"%s\" is already exist!", user.getName())));
            model.addAttribute("roles", roleService.getRoles());
            return "admin/new";
        }

        // Иначе достаем для юзера по указанным именам роли из базы и сохраняем
        user.setRoles(user.getRoles().stream()
                .map(role -> roleService.getByName(role.getName()))
                .collect(Collectors.toSet()));
        userService.saveUser(user);
        return "redirect:/admin";
    }

    @GetMapping("{id}/edit")
    public String editUser(@PathVariable("id") long id, Model model) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getRoles());
        return "admin/edit";
    }

    @PatchMapping("{id}/edit")
    public String updateUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {

        // Если есть ошибки, попросим исправить
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getRoles());
            return "admin/edit";
        }

/*
        // Если ввели не верный пароль, попросим вспомнить
        if (!passwordEncoder.matches(user.getPassword(), userService.getUserById(user.getId()).getPassword())) {
            bindingResult.addError(new FieldError("password", "password", "Wrong password!"));
            model.addAttribute("roles", roleService.getRoles());
            return "admin/edit";
        }
*/

        // Если изменили имя и юзер с таким именем уже существует, сообщим об этом
        if (!user.getName().equals(userService.getUserById(user.getId()).getName()) && userService.getUserByName(user.getName()) != null) {
            bindingResult.addError(new FieldError("name", "name",
                    String.format("User with name \"%s\" is already exist!", user.getName())));
            model.addAttribute("roles", roleService.getRoles());
            return "admin/edit";
        }

        // Иначе достаем для юзера по указанным именам роли из базы и сохраняем
        user.setRoles(user.getRoles().stream()
                .map(role -> roleService.getByName(role.getName()))
                .collect(Collectors.toSet()));
        userService.updateUser(user);
        return "redirect:/admin";
    }

    @DeleteMapping("{id}")
    public String deleteUser(@PathVariable("id") long id) {
        userService.removeUser(id);
        return "redirect:/admin";
    }
}
