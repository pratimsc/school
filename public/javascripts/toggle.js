/*
 * Copyright [2016] Maikalal Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created by pratimsc on 31/01/16.
 */
$(document).ready(function () {

    $("#rate_flat").click(function (event) {
        $("#banded_rate_form").addClass("hidden");
        $("#flat_rate_form").removeClass("hidden");
        event.preventDefault();
    });

    $("#rate_banded").click(function (event) {
        $("#flat_rate_form").addClass("hidden");
        $("#banded_rate_form").removeClass("hidden");
        event.preventDefault();
    });


});
