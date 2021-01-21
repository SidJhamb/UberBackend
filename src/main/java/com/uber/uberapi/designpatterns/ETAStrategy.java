package com.uber.uberapi.designpatterns;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.BookingType;
import com.uber.uberapi.models.ExactLocation;
import lombok.Builder;

import java.util.List;

class SomeController {
    void createBooking() {
        Booking booking = Booking.builder().build();
        BookingContext bookingContext = BookingStrategyFactory.autoConfigure(booking);
        System.out.println(bookingContext.etaStrategy.getETAInMinutes(new ExactLocation(), new ExactLocation()));
    }
}

// {in our application}
// Singleton - spring - by making @Component - by default Spring beans are singletons
// builder
// Strategy
// Factory
// Chain of responsibility


// Chain of responsibility Pattern
// price - booking type
class CombinedPricingStrategy implements PricingStrategy {
    private static List<PriceDelta>  priceDeltaList;
    static {
        priceDeltaList.add(new WeatherPriceDelta());
        priceDeltaList.add(new BookingTypePriceDelta());
    }
    @Override
    public Integer getPriceInRupees(Booking booking) {
        Integer price = 1;
        for(PriceDelta priceDelta: priceDeltaList){
            price = priceDelta.apply(booking, price);
        }
        return price;
    }
}


interface PriceDelta {
    public Integer apply(Booking booking, Integer price);
}

class BookingTypePriceDelta implements PriceDelta {
    @Override
    public Integer apply(Booking booking, Integer price) {
        if(booking.getBookingType().equals(BookingType.Prime)){
            return price * 2;
        }
        else{
            return price;
        }
    }
}

class WeatherPriceDelta implements PriceDelta {
    @Override
    public Integer apply(Booking booking, Integer price) {
//        if(weatherAPI.getCurrentWeather(booking.getRoute().get(0)).isBad()){
//            return price * 5;
//        }
//        else{
//            return price;
//        }
        return null;
    }
}


@Builder
class BookingContext {
    PricingStrategy pricingStrategy;
    ETAStrategy etaStrategy;
} // Configuration

class BookingStrategyFactory {
    public static BookingContext autoConfigure(Booking booking){
        if(booking.getBookingType().equals(BookingType.Prime)){
            return getPrimeStrategy();
        }
        else{
            return getXLStrategy();
        }
    }

    public static BookingContext getPrimeStrategy() {
        return BookingContext.builder()
                .etaStrategy(new PrimeETAStrategy())
                .pricingStrategy((new PrimePricingStrategy()))
                .build();
    }

    public static BookingContext getXLStrategy() {
        return BookingContext.builder()
                .etaStrategy(new PrimeETAStrategy())
                .pricingStrategy((new XLPricingStrategy()))
                .build();
    }

}
interface PricingStrategy {
    public Integer getPriceInRupees(Booking booking);
}

class PrimePricingStrategy implements PricingStrategy {
    @Override
    public Integer getPriceInRupees(Booking booking) {
        return null;
    }
}

class XLPricingStrategy implements PricingStrategy {
    @Override
    public Integer getPriceInRupees(Booking booking) {
        return null;
    }
}

public interface ETAStrategy {
    public Integer getETAInMinutes(ExactLocation start, ExactLocation end);
}

class SimpleETAStrategy implements ETAStrategy {
    @Override
    public Integer getETAInMinutes(ExactLocation start, ExactLocation end) {
        return 10;
    }
}

class PrimeETAStrategy implements ETAStrategy {
    @Override
    public Integer getETAInMinutes(ExactLocation start, ExactLocation end) {
        return 4;
    }
}

class BadWeatherETAStrategy implements ETAStrategy {
    @Override
    public Integer getETAInMinutes(ExactLocation start, ExactLocation end) {
        return 20;
    }
}
