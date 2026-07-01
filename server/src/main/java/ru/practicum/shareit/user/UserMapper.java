package ru.practicum.shareit.user;

public class UserMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static User toUser(UserDto userDto) {
        if (userDto == null) return null;
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }
}
