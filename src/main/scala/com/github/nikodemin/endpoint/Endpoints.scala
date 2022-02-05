package com.github.nikodemin.endpoint

import com.github.nikodemin.model.ContributorInfo
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody

object Endpoints {

  val getContributorsByCompany: Endpoint[Unit, String, Unit, List[ContributorInfo], Any] = endpoint
    .get
    .in("org" / path[String] / "contributors")
    .out(jsonBody[List[ContributorInfo]])
}
