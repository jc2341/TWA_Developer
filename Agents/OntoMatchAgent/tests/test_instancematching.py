import logging

import evaluate
import instancematching
import scoring
import utils_for_testing

class TestInstanceMatching(utils_for_testing.TestCaseOntoMatch):

    def get_params_blocking(self):
        return {
            "name": "TokenBasedPairIterator",
            "model_specific": {
                 "min_token_length": 3,
                 "max_token_occurrences_src": 20,
                 "max_token_occurrences_tgt": 20,
                 "blocking_properties": ["name", "isOwnedBy/hasName", "address/addressLocality", "address/streetAddress", "address/postalCode"],
                 "reset_index": False,
            }
        }

    #TODO-AE replace method by config file
    def get_prop_prop_sim_tuples_without_geo_coordinates(self):
        params_sim_fcts = [{
                    "name": "dist_nltk_edit",
                    "cut_off_mode": "fixed",
                    "cut_off_value": 3
                },{
                    "name": "dist_absolute",
                    "cut_off_mode": "fixed",
                    "cut_off_value": 10
                },{
                    "name": "dist_relative",
                    "cut_off_mode": "fixed"
                },{
                    "name": "dist_equal",
                    "cut_off_mode": "fixed"
                },{
                    "name": "dist_cosine_with_tfidf",
                    "cut_off_mode": "fixed"
            }
        ]
        sim_fcts = scoring.create_similarity_functions_from_params(params_sim_fcts)
        return [
            ('name', 'name', sim_fcts[4]),
            ('isOwnedBy/hasName', 'isOwnedBy/hasName', sim_fcts[0]),
            ('hasYearOfBuilt/hasValue/numericalValue', 'hasYearOfBuilt/hasValue/numericalValue', sim_fcts[1]),
            ('designCapacity/hasValue/numericalValue', 'designCapacity/hasValue/numericalValue', sim_fcts[2]),
            #switched from type to fuel
            #('type', 'type', sim_fcts[3]),
            ('realizes/consumesPrimaryFuel', 'realizes/consumesPrimaryFuel', sim_fcts[3]),
        ]

    def get_prop_prop_sim_tuples_with_geo_coordinates(self):
        params_sim_fcts = [{
                    "name": "dist_nltk_edit",
                    "cut_off_mode": "fixed",
                    "cut_off_value": 3
                },{
                    "name": "dist_absolute",
                    "cut_off_mode": "fixed",
                    "cut_off_value": 10
                },{
                    "name": "dist_relative",
                    "cut_off_mode": "fixed"
                },{
                    "name": "dist_equal",
                    "cut_off_mode": "fixed"
                },{
                    "name": "dist_cosine_with_tfidf",
                    "cut_off_mode": "fixed"
            }
        ]
        sim_fcts = scoring.create_similarity_functions_from_params(params_sim_fcts)
        return [
            ('name', 'name', sim_fcts[4]),
            ('isOwnedBy/hasName', 'isOwnedBy/hasName', sim_fcts[0]),
            ('hasYearOfBuilt/hasValue/numericalValue', 'hasYearOfBuilt/hasValue/numericalValue', sim_fcts[1]),
            ('designCapacity/hasValue/numericalValue', 'designCapacity/hasValue/numericalValue', sim_fcts[2]),
            ('realizes/consumesPrimaryFuel', 'realizes/consumesPrimaryFuel', sim_fcts[3]),
            #TODO-AE long = x ???
            ('geo:wgs84_pos#long', 'hasGISCoordinateSystem/hasProjectedCoordinate_x/hasValue/numericalValue', sim_fcts[2]),
            ('geo:wgs84_pos#lat', 'hasGISCoordinateSystem/hasProjectedCoordinate_y/hasValue/numericalValue', sim_fcts[2])
        ]

    def read_kwl_gppd_DEU_matching_file(self):
        #TODO-AE move this file
        matchfile = 'C:/my/tmp/ontomatch/scores_kwl_20210720_8.csv'
        # TODO-AE URGENT link type 2
        index_set_matches = evaluate.read_match_file_as_index_set(matchfile, linktypes = [1, 2, 3, 4, 5])
        logging.info('ground truth matches=%s', len(index_set_matches))
        return index_set_matches

    def test_auto_calibration_without_geo_coordinates(self):

        params_blocking = self.get_params_blocking()

        prop_prop_sim_tuples = self.get_prop_prop_sim_tuples_without_geo_coordinates()

        src_onto, tgt_onto = self.load_kwl_gppd_ontologies()

        matcher = instancematching.InstanceMatcherWithAutoCalibration()

        df_total_scores, df_total_best_scores = matcher.start(src_onto, tgt_onto, params_blocking, prop_prop_sim_tuples=prop_prop_sim_tuples)

        logging.debug('describe dataset 1:\n%s', matcher.score_manager.get_data1().describe().to_string())
        logging.debug('describe dataset 2:\n%s', matcher.score_manager.get_data2().describe().to_string())

        index_set_matches = self.read_kwl_gppd_DEU_matching_file()
        logging.info('length of total best scores=%s', len(df_total_best_scores))
        result = evaluate.evaluate(df_total_best_scores, index_set_matches, number_of_thresholds=11)

        self.assertEqual(len(df_total_best_scores), 1298)

        # max f1-score=0.8874388254486133 for threshold t=0.29999999999999993
        # area under curve=0.9052648606935376
        expected_result = [[1.0, 1.0, 0.0, 0, 0, 928, 0.0],
            [0.9, 1.0, 0.0, 0, 0, 928, 0.0],
            [0.8, 1.0, 0.0, 0, 0, 928, 0.0],
            [0.7, 1.0, 0.017241379310344827, 16, 0, 912, 0.03389830508474576],
            [0.6, 1.0, 0.30064655172413796, 279, 0, 649, 0.46230323115161565],
            [0.5, 0.9870967741935484, 0.49461206896551724, 459, 6, 469, 0.6590093323761665],
            [0.3999999999999999, 0.9717514124293786, 0.7413793103448276, 688, 20, 240, 0.8410757946210269],
            [0.29999999999999993, 0.8957189901207464, 0.8793103448275862, 816, 95, 112, 0.8874388254486133],
            [0.19999999999999996, 0.7930720145852325, 0.9375, 870, 227, 58, 0.8592592592592592],
            [0.09999999999999998, 0.6798756798756799, 0.9428879310344828, 875, 412, 53, 0.7900677200902935],
            [0.0, 0.6741140215716487, 0.9428879310344828, 875, 423, 53, 0.7861635220125786]]

        for i, expected in enumerate(expected_result):
            actual = result[i]
            self.assertAlmostEqual(actual[1], expected[1], places=2)
            self.assertAlmostEqual(actual[2], expected[2], places=2)

    def test_auto_calibration_with_geo_coordinates(self):

        params_blocking = self.get_params_blocking()

        prop_prop_sim_tuples = self.get_prop_prop_sim_tuples_with_geo_coordinates()

        src_onto, tgt_onto = self.load_kwl_with_geo_coordinates_gppd_ontologies()

        matcher = instancematching.InstanceMatcherWithAutoCalibration()

        df_total_scores, df_total_best_scores = matcher.start(src_onto, tgt_onto, params_blocking, prop_prop_sim_tuples=prop_prop_sim_tuples)

        df_total_scores.to_csv('C:/my/repos/ontomatch_20210924/tmp/total_scores_geo_2.csv')
        df_total_scores.to_csv('C:/my/repos/ontomatch_20210924/tmp/total_best_scores_geo_2.csv')

        logging.debug('describe dataset 1:\n%s', matcher.score_manager.get_data1().describe().to_string())
        logging.debug('describe dataset 2:\n%s', matcher.score_manager.get_data2().describe().to_string())

        index_set_matches = self.read_kwl_gppd_DEU_matching_file()
        logging.info('length of total best scores=%s', len(df_total_best_scores))
        result = evaluate.evaluate(df_total_best_scores, index_set_matches, number_of_thresholds=11)

        self.assertEqual(len(df_total_best_scores), 1298)

        #max f1-score=0.9138022210470651 for threshold t=0.6
        #area under curve=0.9121979005625834
        '''
        expected_result = [[1.0, 1.0, 0.034482758620689655, 32, 0, 896, 0.06666666666666667],
            [0.9, 1.0, 0.11637931034482758, 108, 0, 820, 0.2084942084942085],
            [0.8, 0.9772727272727273, 0.46336206896551724, 430, 10, 498, 0.628654970760234],
            [0.7, 0.9716312056737588, 0.7381465517241379, 685, 20, 243, 0.838946723821188],
            [0.6, 0.897196261682243, 0.9310344827586207, 864, 99, 64, 0.9138022210470651],
            [0.5, 0.7779735682819383, 0.9515086206896551, 883, 252, 45, 0.8560349006301503],
            [0.4, 0.692247454972592, 0.9525862068965517, 884, 393, 44, 0.801814058956916],
            [0.3, 0.6810477657935285, 0.9525862068965517, 884, 414, 44, 0.7942497753818509],
            [0.2, 0.6810477657935285, 0.9525862068965517, 884, 414, 44, 0.7942497753818509],
            [0.1, 0.6810477657935285, 0.9525862068965517, 884, 414, 44, 0.7942497753818509],
            [0.0, 0.6810477657935285, 0.9525862068965517, 884, 414, 44, 0.7942497753818509]]
        '''

        # max f1-score=0.9069003285870756 for threshold t=0.29999999999999993
        # area under curve=0.9104694783118478

        expected_result = [[1.0, 1.0, 0.0, 0, 0, 928, 0.0],
            [0.9, 1.0, 0.0, 0, 0, 928, 0.0],
            [0.8, 1.0, 0.0, 0, 0, 928, 0.0],
            [0.7, 1.0, 0.007543103448275862, 7, 0, 921, 0.014973262032085563],
            [0.6, 1.0, 0.07758620689655173, 72, 0, 856, 0.144],
            [0.5, 0.9940652818991098, 0.3609913793103448, 335, 2, 593, 0.5296442687747035],
            [0.3999999999999999, 0.981679389312977, 0.6928879310344828, 643, 12, 285, 0.8123815540113708],
            [0.29999999999999993, 0.9220489977728286, 0.8922413793103449, 828, 70, 100, 0.9069003285870756],
            [0.19999999999999996, 0.7726872246696035, 0.9450431034482759, 877, 258, 51, 0.8502181289384392],
            [0.09999999999999998, 0.678516228748068, 0.9461206896551724, 878, 416, 50, 0.7902790279027904],
            [0.0, 0.6764252696456087, 0.9461206896551724, 878, 420, 50, 0.788858939802336]]


        for i, expected in enumerate(expected_result):
            actual = result[i]
            self.assertAlmostEqual(actual[1], expected[1], places=2)
            self.assertAlmostEqual(actual[2], expected[2], places=2)

    def test_get_total_score_for_row(self):
        prop_scores = [[1., 0.5, None, 0., 0.5], [None, 0.7, None, 0.3]]
        scoring_weights = [[1., 0., 1., 1., 0.1], [1., 0.5, 0.5, 0.5]]
        expected = [[2., 1.05, 2.4, 2.05, 3/5, 2/4], [1., 0.5, 1.8, 2., 3/4, 0.]]

        for i, scores in enumerate(prop_scores):
            logging.debug('iteration i=%s', i)
            total_score = instancematching.get_total_score_for_row(scores)
            self.assertEqual(total_score, expected[i][0])
            total_score = instancematching.get_total_score_for_row(scores, scoring_weights[i])
            self.assertEqual(total_score, expected[i][1])
            total_score = instancematching.get_total_score_for_row(scores, missing_score=0.4)
            self.assertEqual(total_score, expected[i][2])
            total_score = instancematching.get_total_score_for_row(scores, scoring_weights[i], missing_score=1.)
            self.assertEqual(total_score, expected[i][3])
            total_score = instancematching.get_total_score_for_row(scores, missing_score=1., aggregation_mode='mean', average_min_prop_count=1)
            self.assertEqual(total_score, expected[i][4])
            total_score = instancematching.get_total_score_for_row(scores, aggregation_mode='mean', average_min_prop_count=3)
            self.assertEqual(total_score, expected[i][5])

    def test_matching_with_scoring_weights_without_geo_coordinates(self):

        params = self.read_conf_kwl()
        src_onto, tgt_onto = self.load_kwl_gppd_ontologies()
        params_blocking = params['blocking']
        #params_blocking = self.get_params_blocking()
        params_mapping = params['mapping']
        #prop_prop_sim_tuples = self.get_prop_prop_sim_tuples_without_geo_coordinates()

        matcher = instancematching.InstanceMatcherWithScoringWeights()
        #matcher.start(src_onto, tgt_onto, params_blocking, None, prop_prop_sim_tuples)
        matcher.start(src_onto, tgt_onto, params_blocking, params_mapping)
        scores = matcher.get_scores()
        logging.debug('number=%s', len(scores))
        logging.debug('columns=%s', [ str(c) for c in scores.columns])

        #scoring_weights = [0.3, 0.2, 0.1, 0.2, 0.2]
        #scoring_weights = [0.4, 0.2, 0.1, 0.2, 0.3]
        scoring_weights = [0.8, 0.0, 0.1, 0.0, 0.1]
        weight_sum = sum(scoring_weights)
        scoring_weights = [ w/weight_sum for w in scoring_weights]

        instancematching.add_total_scores(scores, props=[0, 1, 2, 3, 4], scoring_weights=scoring_weights)

        matches = self.read_kwl_gppd_DEU_matching_file()
        result = evaluate.evaluate(scores, matches, number_of_thresholds=11)


    	#max f1-score=0.818840579710145 for threshold t=0.9
        #area under curve=0.8087935118689061
        '''
        expected_result = [[1.0, 0.9941348973607038, 0.36530172413793105, 339, 2, 589, 0.5342789598108747],
            [0.9, 0.9313186813186813, 0.7306034482758621, 678, 50, 250, 0.818840579710145],
            [0.8, 0.7822318526543879, 0.7780172413793104, 722, 201, 206, 0.7801188546731497],
            [0.7, 0.7434869739478958, 0.7995689655172413, 742, 256, 186, 0.7705088265835929],
            [0.6, 0.7180952380952381, 0.8125, 754, 296, 174, 0.7623862487360971],
            [0.5, 0.6836917562724014, 0.822198275862069, 763, 353, 165, 0.7465753424657534],
            [0.4, 0.6303278688524591, 0.8286637931034483, 769, 451, 159, 0.7160148975791434],
            [0.3, 0.5767790262172284, 0.8297413793103449, 770, 565, 158, 0.6805125939019001],
            [0.2, 0.49570552147239266, 0.8706896551724138, 808, 822, 120, 0.6317435496481626],
            [0.1, 0.2594296228150874, 0.9116379310344828, 846, 2415, 82, 0.40391501551682985],
            [0.0, 0.17985611510791366, 0.915948275862069, 850, 3876, 78, 0.30067209055535904]]
        '''

        # max f1-score=0.8265682656826568 for threshold t=0.9
        # area under curve=0.8377025437038117
        '''
        expected_result = [[1.0, 0.993993993993994, 0.3566810344827586, 331, 2, 597, 0.5249801744647106],
        [0.9, 0.9627507163323782, 0.7241379310344828, 672, 26, 256, 0.8265682656826568],
        [0.8, 0.8429073856975381, 0.7747844827586207, 719, 134, 209, 0.8074115665356542],
        [0.7, 0.7932535364526659, 0.7855603448275862, 729, 190, 199, 0.78938819707634],
        [0.6, 0.7648261758691206, 0.8060344827586207, 748, 230, 180, 0.7848898216159496],
        [0.5, 0.7119924457034938, 0.8125, 754, 305, 174, 0.7589330649219929],
        [0.4, 0.6576889661164205, 0.8157327586206896, 757, 394, 171, 0.7282347282347282],
        [0.3, 0.5952941176470589, 0.8178879310344828, 759, 516, 169, 0.6890603722197004],
        [0.2, 0.5133587786259542, 0.8696120689655172, 807, 765, 121, 0.6456000000000001],
        [0.1, 0.2685543964232489, 0.9709051724137931, 901, 2454, 27, 0.4207331309829559],
        [0.0, 0.16903249906611878, 0.9752155172413793, 905, 4449, 23, 0.2881248010187838]]
        '''

        # max f1-score=0.8181268882175227 for threshold t=0.9
        # area under curve=0.8232895636412483
        expected_result = [[1.0, 0.9941002949852508, 0.36314655172413796, 337, 2, 591, 0.531965272296764],
            [0.9, 0.9312242090784044, 0.7295258620689655, 677, 50, 251, 0.8181268882175227],
            [0.8, 0.7903402854006586, 0.7758620689655172, 720, 191, 208, 0.7830342577487764],
            [0.7, 0.7626943005181347, 0.7931034482758621, 736, 229, 192, 0.7776016904384575],
            [0.6, 0.7350928641251222, 0.8103448275862069, 752, 271, 176, 0.7708867247565351],
            [0.5, 0.6988950276243094, 0.8178879310344828, 759, 327, 169, 0.7537239324726912],
            [0.3999999999999999, 0.6507258753202391, 0.8211206896551724, 762, 409, 166, 0.7260600285850404],
            [0.29999999999999993, 0.5889145496535797, 0.8243534482758621, 765, 534, 163, 0.6870229007633588],
            [0.19999999999999996, 0.5072463768115942, 0.8674568965517241, 805, 782, 123, 0.6401590457256461],
            [0.09999999999999998, 0.26840215439856374, 0.9665948275862069, 897, 2445, 31, 0.4201405152224824],
            [0.0, 0.16828358208955224, 0.9719827586206896, 902, 4458, 26, 0.2868956743002545]]

        # TODO-AE 211030 Is there some randomness in this test. Result seems to change by accident
        for i, expected in enumerate(expected_result):
            actual = result[i]
            self.assertAlmostEqual(actual[1], expected[1], places=2)
            self.assertAlmostEqual(actual[2], expected[2], places=2)
