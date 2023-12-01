package com.grt.milleniumfalcon.repository;

import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.model.RouteCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, RouteCompositeId> {
}
