package ru.maverick.cardmanagementsystem.card.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.maverick.cardmanagementsystem.card.dto.CardDto;
import ru.maverick.cardmanagementsystem.card.model.Card;


@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "maskedNumber", source = "lastFourDigits", qualifiedByName = "maskCardNumber")
    @Mapping(target = "ownerName", source = "owner.fullName")
    CardDto toDto(Card card);

    /*@Mapping(target = "cardNumber", source = "cardNumber", qualifiedByName = "maskCardNumber")
    CardShortDto toShortDto(Card card);*/

    @Named("maskCardNumber")
    default String mapLastFourToMasked(String lastFourDigits) {
        return "**** **** **** " + lastFourDigits;
    }
}
