/*!
    * Start Bootstrap - Freelancer v6.0.4 (https://startbootstrap.com/themes/freelancer)
    * Copyright 2013-2020 Start Bootstrap
    * Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-freelancer/blob/master/LICENSE)
    */

// TODO: separate the visualization for JPS/ wolfram + google ...
// ============= add the auto-completion function ========

function autocomplete(inp, arr) {
  /*the autocomplete function takes two arguments,
  the text field element and an array of possible autocompleted values:*/
  var currentFocus;
  /*execute a function when someone writes in the text field:*/
  inp.addEventListener("input", function(e) {
      var a, b, i, val = this.value;
       /*close any already open lists of autocompleted values*/
      closeAllLists();
      if (!val) { return false;}
      components = val.split(' ')
      val = components[components.length - 1]
      // if val === ' ' {return false; }
      currentFocus = -1;
      if (val === '') {return false;}
      /*create a DIV element that will contain the items (values):*/
      a = document.createElement("DIV");
      a.setAttribute("id", this.id + "autocomplete-list");
      a.setAttribute("class", "autocomplete-items");
      /*append the DIV element as a child of the autocomplete container:*/
      this.parentNode.appendChild(a);
      /*for each item in the array...*/
      for (i = 0; i < arr.length; i++) {
        /*check if the item starts with the same letters as the text field value:*/
        if (arr[i].substr(0, val.length).toUpperCase() == val.toUpperCase()) {
          /*create a DIV element for each matching element:*/
          b = document.createElement("DIV");
          /*make the matching letters bold:*/
          b.innerHTML = "<strong>" + arr[i].substr(0, val.length) + "</strong>";
          b.innerHTML += arr[i].substr(val.length);
          /*insert a input field that will hold the current array item's value:*/
          b.innerHTML += "<input type='hidden' value='" + arr[i] + "'>";
          /*execute a function when someone clicks on the item value (DIV element):*/
          b.addEventListener("click", function(e) {
              /*insert the value for the autocomplete text field:*/
              inp.value = inp.value.replace(val,'') +  this.getElementsByTagName("input")[0].value;
              /*close the list of autocompleted values,
              (or any other open lists of autocompleted values:*/
              closeAllLists();
          });
          a.appendChild(b);
        }
      }
  });
  /*execute a function presses a key on the keyboard:*/
  inp.addEventListener("keydown", function(e) {
      var x = document.getElementById(this.id + "autocomplete-list");
      if (x) x = x.getElementsByTagName("div");
      if (e.keyCode == 40) {
        /*If the arrow DOWN key is pressed,
        increase the currentFocus variable:*/
        currentFocus++;
        /*and and make the current item more visible:*/
        addActive(x);
      } else if (e.keyCode == 38) { //up
        /*If the arrow UP key is pressed,
        decrease the currentFocus variable:*/
        currentFocus--;
        /*and and make the current item more visible:*/
        addActive(x);
      } else if (e.keyCode == 13) {
        /*If the ENTER key is pressed, prevent the form from being submitted,*/
        e.preventDefault();
        if (currentFocus > -1) {
          /*and simulate a click on the "active" item:*/
          if (x) x[currentFocus].click();
        }
      }
  });
  function addActive(x) {
    /*a function to classify an item as "active":*/
    if (!x) return false;
    /*start by removing the "active" class on all items:*/
    removeActive(x);
    if (currentFocus >= x.length) currentFocus = 0;
    if (currentFocus < 0) currentFocus = (x.length - 1);
    /*add class "autocomplete-active":*/
    x[currentFocus].classList.add("autocomplete-active");
  }
  function removeActive(x) {
    /*a function to remove the "active" class from all autocomplete items:*/
    for (var i = 0; i < x.length; i++) {
      x[i].classList.remove("autocomplete-active");
    }
  }
  function closeAllLists(elmnt) {
    /*close all autocomplete lists in the document,
    except the one passed as an argument:*/
    var x = document.getElementsByClassName("autocomplete-items");
    for (var i = 0; i < x.length; i++) {
      if (elmnt != x[i] && elmnt != inp) {
        x[i].parentNode.removeChild(x[i]);
      }
    }
  }
  /*execute a function when someone clicks in the document:*/
  document.addEventListener("click", function (e) {
      closeAllLists(e.target);
  });
}



// input-field
autocomplete(document.getElementById("input-field"), species);







function get_random_question(){

const index = Math.floor(Math.random() * vibration_frequency.length);

    qst = '  show me the vibration frequency of ' + vibration_frequency[index]
    document.getElementById('input-field').value = qst;

}

let local_address = 'http://127.0.0.1:5000/'
let cmcl_address = 'https://kg.cmclinnovations.com/'
let address = cmcl_address

	$(window).on('load', function(){

	    let hostname = location.hostname;
	    console.log('host name is', hostname)

	    if (hostname.includes('127.0.0.1')){
	        address = local_address
	    }else{
	        address = cmcl_address
	    }
	    console.log('address is set to be', address)
	    google.charts.load('current', {'packages':['table']});
	    $('#google_result_box').hide()
	    $('#wolfram_result_box').hide()
	    $('#progress-container').hide()
	});

    (function($) {
    "use strict"; // Start of use strict

    // Smooth scrolling using jQuery easing
    $('a.js-scroll-trigger[href*="#"]:not([href="#"])').click(function() {
      if (location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '') && location.hostname == this.hostname) {
        var target = $(this.hash);
        target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');
        if (target.length) {
          $('html, body').animate({
            scrollTop: (target.offset().top - 71)
          }, 1000, "easeInOutExpo");
          return false;
        }
      }
    });

    // Scroll to top button appear
    $(document).scroll(function() {
      var scrollDistance = $(this).scrollTop();
      if (scrollDistance > 100) {
        $('.scroll-to-top').fadeIn();
      } else {
        $('.scroll-to-top').fadeOut();
      }
    });

    // Closes responsive menu when a scroll trigger link is clicked

    $('.sample-question').click(function(){
           // ask_question()
           let q =  $(this).text().replace('   ',' ');
           document.getElementById('input-field').value = q;
           $('html,body').scrollTop(0);

           // ask_question()
    });



    $('.js-scroll-trigger').click(function() {
      $('.navbar-collapse').collapse('hide');
    });

    // Activate scrollspy to add active class to navbar items on scroll
    $('body').scrollspy({
      target: '#mainNav',
      offset: 80
    });

    // Collapse Navbar
    var navbarCollapse = function() {
      if ($("#mainNav").offset().top > 100) {
        $("#mainNav").addClass("navbar-shrink");
      } else {
        $("#mainNav").removeClass("navbar-shrink");
      }
    };
    // Collapse now if page is not at top
    navbarCollapse();
    // Collapse the navbar when page is scrolled
    $(window).scroll(navbarCollapse);

  })(jQuery);

    var socket = io();

    socket.on('connect', function() {
        socket.emit('message', {data: 'I\'m connected!'});
    });

    socket.on('coordinate_agent', function(msg) {
        console.log('from coordinate agent', msg)
        update_log(msg)
    });


  var progress_counter = 1

  function update_log(msg){
     // TODO: update the log info
     if (msg.includes('Querying') && msg.includes('The World Avatar Knowledge Graph')){
        $('#query_progress').append('<div>' + msg + '</div>')
        console.log('updating the progress bar item ')
        $('#query_progress_bar').empty()
        $('#query_progress_bar').show()
        $('#query_progress').append('<div id="query_progress_bar">' + '#' + '</div>')

        setInterval(function(){

            progress_counter = progress_counter + 1;
            bar = '#'
            for (var i = 1; i < progress_counter; i++)
            {

                if (progress_counter < 20){
                    bar = bar + '#'
                }else{
                    progress_counter = 1
                }
            }
            console.log('progress bar', progress_counter)
            console.log('bar', bar)
            $('#query_progress_bar').html(bar)

         },2000);


     }
     else{
        $('#query_progress').append('<div>' + msg + '</div>')
        console.log('updating the progress', msg)
     }
  }




  function ask_question() {

    $('#query_progress').empty()
    $('#progress-container').show()

    document.getElementById('search-icon').style.display = 'none';
    document.getElementById('search-spinner').style.display = 'block';

    // =========================
    msg = $('#input-field').val();
    //$('#input-field').val(null);
    // =========================
    msg = msg.replace(/[/+]/g, 'add_sign')

   // to test the code locally, the address need to be changed to  http://127.0.0.1:5000/


    $('#search-results').hide()
    $('#google_result_box').hide()
    $('#wolfram_result_box').hide()


    query_wolfram_alpha(address, msg);
    query_google(address, msg);

    $.get(address + "query?question=" + msg, function( data ) {
      displayResults(data, 'jps')
    });

}



function process_json_result(result){

  // result = result.replace(/=\]/g, '=>').replace(/[}][\n ]+[{]/g, '},{')
  console.log('The request has returned a response ', result)
  result = JSON.parse(result)
  console.log('the result parsed', result, typeof(result) )


  if (result === 'Nothing'){
    console.log('Received nothing')
    update_log('The World Avatar failed to provide an answer')
    $('#query_progress_bar').html('')


    query_wolfram_alpha(address, msg);
    query_google(address, msg);
    return null
  }




  console.log('If it is nothing, you should not see this line')


  if (result){
      console.log('the result parsed', result, typeof(result) )
    if (typeof(result)!== 'object' && (result!== 'Nothing')){
      obj = '{"results": ' + result + '}'
      console.log(obj)
      r = JSON.parse(obj)
      console.log('the array',  r["results"])
        keys = []
        table = []
         console.log('this is a result from JPS', r)
         // get the variable names
         first_row = r["results"][0]
         head_object = {'result_id': 'index'}
         for (let head in first_row){
            head_object[head] = head
         }
         table.push(head_object)

        r["results"].forEach(function (item, index) {
         let row_object = {}


           for (let key in item) {
                 console.log(key, item[key]);
           counter = index + 1
           console.log(item, index);
           row_object['result_id'] = counter.toString()
           row_object[key] = item[key]
           }
            table.push(row_object)

        });
          console.log('------------- jps table ---------------')
          console.log(table)
    return table
  }


    if ('results' in result && !('Nothing' in result)){
    bindings = result.results.bindings;
    if (bindings.length == 0){
        // make a request to google or wolfram alpha
        return null
    }else

	{
        variables = result.head.vars
        table = []
		index_counter = 0
        bindings.forEach(function(v){
            let row = []
			let row_object = {}
			index_counter++
            row_object['result_id'] = index_counter.toString()

            if (v['oLabel']){
            			row_object['result_name'] = v['oLabel']['value']
            }

             if (v['name']){
            			row_object['result_name'] = v['name']['value']
            }


               if (v['v']){
			row_object['result_value'] = v['v']['value']
            }


			if (v['v2'])
		    {
		        row_object['result_value_2'] = v['v2']['value']
		    }

            if (v['unitLabel'])

		    {
			    row_object['result_unit'] = v['unitLabel']['value']
		    }


            table.push(row_object)
        })

		console.log('table', table)
        return table
    }
  }else{
  // get a list of variables, which is the keys
  variables = Object.keys(result[0]);
  console.log('variables', variables)
  index_counter = 0
  result.forEach(function(v){
      row = Object.values(v)
	  let row_obj = {}
	  row_obj['result_id'] = index_counter.toString()
	  row_obj['result_name'] = v
	  row_obj['result_value'] = row
      table.push(row_obj)
  })
  console.log('------------- table ---------------')
  console.log(table)
  return table
  }
  }

  else{
    // call wolfram_alpha or google
    console.log('No valid result returned')
  }
}
// if the query to the JPS fails, the system queries both wolfram_alpha and google at the same time
function query_wolfram_alpha(address, msg){
    $.get(address + "query_wolfram?question=" + msg, function( data ) {
      visualize_wolfram_result(data, 'wolfram')
    });
}

function query_google(address, msg){
    // the result returned by google will be in the form of html divisions, the visualization will be different
        $.get(address + "query_google?question=" + msg, function( data ) {
         visualize_google_result(data, 'google')
    });
}



function visualize_google_result(result){
    $('#google_result_box').show()
    if (result.trim() === ''){
        $("#google-results" ).html('<div class="div-row">Google failed to provide a direct answer</div>')
        $('#query_progress').append('<div>Google failed to provide a direct answer</div>')
    }else{
        //div = '<div class="div-row">' + result + '</div>'
        div = result
        $("#google-results" ).html(div)
    }
}

function visualize_wolfram_result(result){
    $('#wolfram_result_box').show()
        if (result.trim() === ''){
        $("#wolfram-results" ).html('<div class="div-row">Google failed to provide a direct answer</div>')
        $('#query_progress').append('<div>Wolfram alpha failed to provide a direct answer</div>')
    }else{
        div = '<div class="div-row">' + result + '</div>'
        $("#wolfram-results" ).html(div)
    }
   }


// TODO: query wolfram alpha and google no matter what
// TODO: Make the page Marie Curie
function removeItemAll(arr, value) {
  var i = 0;
  while (i < arr.length) {
      if(arr[i] === value) {
          arr.splice(i, 1);
      } else {
          ++i;
      }
  }
  return arr;
}

function drawTable(result_array) {
  console.log('result array', result_array)
  variables = result_array[0]
  rows = result_array[1]
  console.log('rows received', rows);
  first_col = rows[0];
  col_size = first_col.length;

  console.log('col size', col_size)
  var data = new google.visualization.DataTable();

/*
  if ((rows.length == 1) && (col_size == 1))
  {
      $('#single_div').val(rows[0][0])

  } */
  //else{
  for (col = 0; col < col_size; col++)
  {
    data.addColumn('string', variables[col]);
    console.log('added col' + variables[col])
  }

  data.addRows(rows);
  table_element = document.getElementById('table_div')
  // table_element.style.color = 'black';
  var table = new google.visualization.Table(table_element);

  var options =
  {
    showRowNumber: false,
    width: '100%',
    height: '100%',
    alternatingRowStyle: false,
    allowHtml: true,
    cssClassNames: {
      tableCell: 'cell',
      headerCell: 'headerCell'
    }
  };
  table.draw(data, options);



//  }

}

function displayResults(myData, source) {
     myData = process_json_result(myData)

  $('#query_progress_bar').html('')
  $('#query_progress_bar').hide()
  update_log('Obtained result from the World Avatar KG')

  // EXTRACT VALUE FOR HTML HEADER.
  // ('Book ID', 'Book Name', 'Category' and 'Price')
  var col = [];
  for (var i = 0; i < myData.length; i++) {
      for (var key in myData[i]) {
          if (col.indexOf(key) === -1) {
              col.push(key);
          }
      }
  }

  var divContainer = document.getElementById("search-results");
  divContainer.innerHTML = "";

  var h = document.createElement("H1")                // Create a <h1> element
  h.setAttribute("id", "result");
  if (source == 'wolfram'){
    h.innerHTML = 'Results (from wolfram alpha)'
  }
  else{
    var t = document.createTextNode("Results (from The World Avatar)");     // Create a text node
  h.appendChild(t);
  }

  divContainer.appendChild(h);

  // ADD JSON DATA TO THE TABLE AS ROWS.
  for (var i = 0; i < myData.length; i++) {

      var div_row = document.createElement("div");
      div_row.classList.add('div-row');

      for (var j = 0; j < col.length; j++) {
        // Create the list item:
        var div_inner = document.createElement('div');


        var data = myData[i][col[j]];





        if (data.includes('.svg') || data.includes('.png')){
//            var myImage = $('<img/>');
//            myImage.attr('src', data);

            div_inner.innerHTML = '<img src="' + data + '" style="width:250px">'


        }
        else{

                    if (data.includes('.g09') || data.includes('.xml')){
                    div_inner.innerHTML = '<a href="'+ data +'">'+ data +'</a>'
                // <a href="url">link text</a>
        }else{


                div_inner.appendChild(document.createTextNode(data));
}
        }

        // Set its contents:

        // Add it to the list:

        div_row.appendChild(div_inner);
      }
      divContainer.appendChild(div_row);
  }

  // FINALLY ADD THE NEWLY CREATED TABLE WITH JSON DATA TO A CONTAINER.

  document.getElementById('search-spinner').style.display = 'none';
  document.getElementById("search-results").style.display = "block";
  document.getElementById('search-icon').style.display = '';

// TODO: install javascript plugin in Pycharm ...
// No, this feature is for Pro ...

};