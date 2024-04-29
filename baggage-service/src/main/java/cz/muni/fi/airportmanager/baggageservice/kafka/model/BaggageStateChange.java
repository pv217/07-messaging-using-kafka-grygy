package cz.muni.fi.airportmanager.baggageservice.kafka.model;

import cz.muni.fi.airportmanager.baggageservice.model.BaggageStatus;

public class BaggageStateChange {

    // TODO add baggageId, passengerId and newStatus fields
    // TODO add non-args constructor and constructor with all fields
    // TODO copy this class to passenger-service along with BaggageStatus enum

    public Long baggageId;
    public Long passengerId;
    public BaggageStatus newStatus;

    public BaggageStateChange() {
    }

    public BaggageStateChange(Long baggageId, Long passengerId, BaggageStatus newStatus) {
        this.baggageId = baggageId;
        this.passengerId = passengerId;
        this.newStatus = newStatus;
    }

    @Override
    public String toString() {
        return "BaggageStateChange{" +
                "baggageId=" + baggageId +
                ", passengerId=" + passengerId +
                ", newStatus=" + newStatus +
                '}';
    }
}
