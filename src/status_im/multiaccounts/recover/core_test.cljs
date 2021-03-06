(ns status-im.multiaccounts.recover.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.multiaccounts.recover.core :as models]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.utils.security :as security]
            [status-im.i18n :as i18n]))

;;;; helpers


(deftest check-phrase-warnings
  (is (= :t/required-field (models/check-phrase-warnings ""))))

;;;; handlers

(deftest set-phrase
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty olympic fat quit display velvet unveil marine crater"))))
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "Game buzz method pretty Olympic fat quit DISPLAY velvet unveil marine crater"))))
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty zeus fat quit display velvet unveil marine crater"))))
  (is (= {:db {:intro-wizard {:passphrase            "   game\t  buzz method pretty olympic fat quit\t   display velvet unveil marine crater  "
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "   game\t  buzz method pretty olympic fat quit\t   display velvet unveil marine crater  "))))
  (is (= {:db {:intro-wizard {:passphrase            "game buzz method pretty 1234 fat quit display velvet unveil marine crater"
                              :passphrase-error      nil
                              :next-button-disabled? false}}}
         (models/set-phrase {:db {}} (security/mask-data "game buzz method pretty 1234 fat quit display velvet unveil marine crater")))))

(deftest store-multiaccount
  (let [new-cofx (models/store-multiaccount {:db {:intro-wizard
                                                  {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                                   :password   "thisisapaswoord"}}})]
    (is (::multiaccounts.create/store-multiaccount new-cofx))))

(deftest recover-multiaccount-with-checks
  (let [new-cofx (models/recover-multiaccount-with-checks {:db {:intro-wizard
                                                                {:passphrase "game buzz method pretty olympic fat quit display velvet unveil marine crater"
                                                                 :password   "thisisapaswoord"}}})]
    (is (::multiaccounts.create/store-multiaccount new-cofx)))
  (let [new-cofx (models/recover-multiaccount-with-checks {:db {:intro-wizard
                                                                {:passphrase "game buzz method pretty zeus fat quit display velvet unveil marine crater"
                                                                 :password   "thisisapaswoord"}}})]
    (is (= (i18n/label :recovery-typo-dialog-title) (-> new-cofx :ui/show-confirmation :title)))
    (is (= (i18n/label :recovery-typo-dialog-description) (-> new-cofx :ui/show-confirmation :content)))
    (is (= (i18n/label :recovery-confirm-phrase) (-> new-cofx :ui/show-confirmation :confirm-button-text)))))

(deftest on-import-multiaccount-success
  (testing "importing a new multiaccount"
    (let [res (models/on-import-multiaccount-success
               {:db {:multiaccounts/multiaccounts {:acc1 {}}}}
               {:key-uid :acc2}
               nil)]
      (is (nil? (:utils/show-confirmation res)))))
  (testing "importing an existing multiaccount"
    (let [res (models/on-import-multiaccount-success
               {:db {:multiaccounts/multiaccounts {:acc1 {}}}}
               {:key-uid :acc1}
               nil)]
      (is (contains? res :utils/show-confirmation)))))
